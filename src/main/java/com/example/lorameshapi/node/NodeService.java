package com.example.lorameshapi.node;

import com.example.lorameshapi.data.Message;
import com.example.lorameshapi.data.MessageUtil;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class NodeService {

    private final Logger logger = LoggerFactory.getLogger(NodeService.class);

    private static final int counterLimit = 32;
    private final NodeRepository nodeRepository;

    public Node resolveNodeId(int nodeId) {
        return nodeRepository.findByNodeId(nodeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "node id " + nodeId + " not found"));
    }

    public Node getById(long id) {
        return nodeRepository.findById(id).orElseGet(() -> {
            logger.info("creating node: "+id);
            Node created = new Node();
            created.setId(id);
            created.setLastUpdated(System.currentTimeMillis());
            created.setStatus(NodeStatus.Down);
            created.setNodeId(-1);
            return nodeRepository.save(created);
        });
    }

    public void put(NodeInfo data) {
        logger.info("updating node "+data.getSerialId()+": " + data.getStatus());
        Node entity = getById(data.getSerialId());
        entity.setStatus(data.getStatus());
        entity.setLastUpdated(System.currentTimeMillis());
        entity.getRetx().replaceAll((k,v) -> 0.0);
        entity.getRetx().putAll(data.getRetx());
        nodeRepository.save(entity);
    }

    public List<String> feed(Message message) {
        logger.info("message feed: "+message.getHeader() + Arrays.toString(message.getData()));
        int header = message.getHeader();
        int nodeId = MessageUtil.nodeId(header);

        List<String> controllerCommands = new ArrayList<>();
        var lost = registerAndListLosses(header);
        if (lost.size() > 0) {
            StringBuilder job = new StringBuilder(String.format("%d trace", nodeId));
            for (int i : lost) job.append(" ").append(i);
            controllerCommands.add(job.toString());
        }

        if (nodeId == 0 || MessageUtil.isJoin(header)) {
            long id = Long.parseLong(new String(message.getData()).substring(1));
            int assignedId = allocateNodeId(id);
            controllerCommands.add(String.format("%d invite %d %d", nodeId, assignedId, id));
        } else if (MessageUtil.isRetx(header)) {
            var updates = updateRouting(message);
            StringBuilder job = new StringBuilder(String.format("%d update", nodeId));
            for (int i : updates) job.append(" ").append(i);
            controllerCommands.add(job.toString());
        }

        return controllerCommands;
    }

    public void putRetx(int nodeId, int otherId, double retx) {
        Node entity = resolveNodeId(nodeId);
        entity.getRetx().put(otherId, retx);
        nodeRepository.save(entity);
    }

    public int allocateNodeId(long id) {
        logger.info("allocating node id: "+id);
        Node entity = getById(id);
        if (entity.getNodeId() > 0) return entity.getNodeId();
        int nodeId = 1;
        while (nodeRepository.existsByNodeId(nodeId)) nodeId++;
        if (nodeId > 63) throw new IllegalStateException("node ids exhausted");
        entity.setNodeId(nodeId);
        entity.setStatus(NodeStatus.Joining);
        entity.setLastUpdated(System.currentTimeMillis());
        nodeRepository.save(entity);
        logger.info("allocated node id: "+nodeId);
        return nodeId;
    }

    public int getCorrespondenceCounter(int nodeId, boolean increment) {
        var entity = resolveNodeId(nodeId);
        int counter = entity.getSendingCounter();
        if (increment) {
            entity.setSendingCounter((counter + 1) % counterLimit);
            nodeRepository.save(entity);
        }
        return counter;
    }

    public Collection<Integer> registerAndListLosses(int header) {
        int nodeId = MessageUtil.nodeId(header);
        int counter = MessageUtil.counter(header);
        var entity = resolveNodeId(nodeId);

        if (counter == entity.getNextReceivingCounter()) {
            entity.setNextReceivingCounter((counter + 1) % counterLimit);
            entity = nodeRepository.save(entity);
        } else if (entity.getMissingMessages().contains(counter)) {
            entity.getMissingMessages().remove(counter);
            nodeRepository.save(entity);
            return Set.of(counter | 64);
        } else {
            for (int i = entity.getNextReceivingCounter(); i != counter; i = ((i + 1) % counterLimit)) {
                entity.getMissingMessages().add(i);
            }
            entity.setNextReceivingCounter((counter + 1) % counterLimit);
            entity = nodeRepository.save(entity);
        }
        return entity.getMissingMessages();
    }

    public List<Node> query() {
        return nodeRepository.findAll();
    }

    Set<Integer> updateRouting(Message message) {
        Node node = resolveNodeId(MessageUtil.nodeId(message.getHeader()));
        node.setStatus(NodeStatus.Node);
        node.setLastUpdated(System.currentTimeMillis());
        node.getRetx().putAll(MessageUtil.retx(message.getData()));
        node = nodeRepository.saveAndFlush(node);

        Set<Integer> current = node.getRouting();
        Set<Integer> calculated = runFloydWarshall(node);
        Set<Integer> updates = new HashSet<>();
        updates.addAll(calculated.stream().filter(i -> !current.contains(i)).toList());
        updates.addAll(current.stream().filter(i -> !calculated.contains(i)).map(i -> i | 64).toList());
        return updates;
    }



    Set<Integer> runFloydWarshall(Node nodeToUpdate) {
        long cutoff = System.currentTimeMillis() - 500000;
        var all = nodeRepository.findAllByLastUpdatedGreaterThanEqual(cutoff);
        var controllers = all.stream().filter(n -> n.getStatus() == NodeStatus.Controller).toList();

        if (controllers.isEmpty()) {
            logger.warn("no controller found");
            return Set.of();
        }

        Map<Integer, Node> byNodeId = new HashMap<>();
        all.forEach(node -> byNodeId.put(node.getNodeId(), node));

        all.forEach(node -> {
            Map<Node, Double> dist = new HashMap<>();
            node.setDistance(dist);
            dist.put(node, 0.0);

            Map<Node, Node> trace = new HashMap<>();
            node.setTrace(trace);
            trace.put(node, node);

            node.setUplinkRouting(new HashSet<>());
            node.setDownlinkRouting(new HashSet<>());

            node.getRetx().forEach((key, value) -> {
                if (value > 0.1) {
                    Node v = byNodeId.get(key);
                    dist.put(v, 1 / value);
                    trace.put(v, v);
                }
            });
        });

        // find all shortest paths
        for (Node k : all) {
            for (Node u : all) {
                for (Node v : all) {
                    if (distance(u, k) + distance(k, v) < distance(u, v)) {
                        u.getDistance().put(v, distance(u, k) + distance(k, v));
                        u.getTrace().put(v, k);
                    }
                }
            }
        }


        for (Node u : all) {
            Node uplinkCtl = null;
            Node downlinkCtl = null;
            for (Node ctl : controllers) {
                if (ctl == u) {
                    uplinkCtl = downlinkCtl = u;
                    break;
                }
                if (uplinkCtl == null || distance(u, ctl) < distance(u, uplinkCtl)) {
                    uplinkCtl = ctl;
                }
                if (downlinkCtl == null || distance(ctl, u) < distance(downlinkCtl, u)) {
                    downlinkCtl = ctl;
                }
            }

            for (Node v = u.getTrace().get(uplinkCtl); v != uplinkCtl; v = v.getTrace().get(uplinkCtl))
                v.getUplinkRouting().add(u);

            for (Node v = downlinkCtl.getTrace().get(u); v != u; v = v.getTrace().get(u))
                v.getDownlinkRouting().add(u);
        }

        Set<Integer> calculatedRouting = new HashSet<>();
        calculatedRouting.addAll(nodeToUpdate.getUplinkRouting().stream().map(Node::getNodeId).collect(Collectors.toSet()));
        calculatedRouting.addAll(nodeToUpdate.getDownlinkRouting().stream().map(Node::getNodeId).map(i -> i | 128).collect(Collectors.toSet()));
        return calculatedRouting;
    }

    private double distance(Node u, Node v) {
        return u.getDistance().getOrDefault(v, Double.MAX_VALUE);
    }
}
