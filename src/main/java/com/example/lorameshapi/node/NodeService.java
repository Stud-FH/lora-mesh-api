package com.example.lorameshapi.node;

import com.example.lorameshapi.data.Message;
import com.example.lorameshapi.util.MessageUtil;
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
    private final NodeRepository nodeRepository;

    public Collection<Node> liveNodes() {
        long threshold = System.currentTimeMillis() - 500000;
        return nodeRepository.findAllByLastUpdatedGreaterThanEqual(threshold);
    }

    public Node resolveAddress(int address) {
        return nodeRepository.findByAddress(address & MessageUtil.ADDRESS_MASK).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "address " + address + " not found"));
    }

    public Node getById(long id) {
        return nodeRepository.findById(id).orElseGet(() -> {
            logger.info("creating node: "+id);
            Node created = new Node();
            created.setId(id);
            created.setLastUpdated(System.currentTimeMillis());
            created.setStatus(NodeStatus.Down);
            created.setAddress(-1);
            return nodeRepository.save(created);
        });
    }

    public void put(NodeInfo data) {
        logger.info("updating node "+data.getId()+": " + data.getStatus());
        Node entity = getById(data.getId());
        entity.setStatus(data.getStatus());
        entity.setLastUpdated(System.currentTimeMillis());
        entity.getRetx().replaceAll((k,v) -> 0.0);
        entity.getRetx().putAll(data.getRetx());
        nodeRepository.save(entity);
    }

    public List<String> feed(Message message, long controllerId) {
        logger.info("message feed: "+message.getHeader() + Arrays.toString(message.getData()));
        int header = message.getHeader();
        int address = MessageUtil.address(header);

        List<String> controllerCommands = new ArrayList<>();
        var lost = registerAndListLosses(header);
        if (lost.size() > 0) {
            StringBuilder job = new StringBuilder(String.format("%d trace", address));
            for (int i : lost) job.append(" ").append(i);
            controllerCommands.add(job.toString());
        }

        if (address == 0 || MessageUtil.isJoin(header)) {
            var data = MessageUtil.joinData(message.getData());
            int allocateAddress = allocateAddress(data.id());
            controllerCommands.add(String.format("%d invite %d %d", address, allocateAddress, data.id()));
        } else if (MessageUtil.isRetx(header)) {
            var updates = updateRouting(message, controllerId);
            StringBuilder job = new StringBuilder(String.format("%d update", address));
            for (int i : updates) job.append(" ").append(i);
            controllerCommands.add(job.toString());
        }

        return controllerCommands;
    }

    public void putRetx(int address, int otherId, double retx) {
        Node entity = resolveAddress(address);
        entity.getRetx().put(otherId, retx);
        nodeRepository.save(entity);
    }

    public int allocateAddress(long id) {
        Node entity = getById(id);
        int address = entity.getAddress();
        if (address > 0) {
            logger.info(String.format("resolved address %d of node %d", address, id));
            return address;
        }
        logger.info("allocating address for node: "+id);
        address = 1;
        while (nodeRepository.existsByAddress(address)) address++;
        if (address > MessageUtil.ADDRESS_LIMIT) throw new IllegalStateException("node ids exhausted");
        entity.setAddress(address);
        entity.setStatus(NodeStatus.Joining);
        entity.setLastUpdated(System.currentTimeMillis());
        nodeRepository.save(entity);
        logger.info(String.format("assigning address %d to node %d", address, id));
        return address;
    }

    public int getCorrespondenceCounter(int address, boolean increment) {
        var entity = resolveAddress(address);
        int counter = entity.getSendingCounter();
        if (increment) {
            entity.setSendingCounter((counter + 1) % MessageUtil.COUNTER_LIMIT);
            nodeRepository.save(entity);
        }
        return counter;
    }

    public Collection<Integer> registerAndListLosses(int header) {
        int address = MessageUtil.address(header);
        int counter = MessageUtil.counter(header);
        var entity = resolveAddress(address);

        if (counter == entity.getNextReceivingCounter()) {
            entity.setNextReceivingCounter((counter + 1) % MessageUtil.COUNTER_LIMIT);
            entity = nodeRepository.save(entity);
        } else if (entity.getMissingMessages().contains(counter)) {
            entity.getMissingMessages().remove(counter);
            nodeRepository.save(entity);
            return Set.of(counter | MessageUtil.DELETE_BIT);
        } else {
            for (int i = entity.getNextReceivingCounter(); i != counter; i = ((i + 1) % MessageUtil.COUNTER_LIMIT)) {
                entity.getMissingMessages().add(i);
            }
            entity.setNextReceivingCounter((counter + 1) % MessageUtil.COUNTER_LIMIT);
            entity = nodeRepository.save(entity);
        }
        return entity.getMissingMessages();
    }

    public List<Node> query() {
        return nodeRepository.findAll();
    }

    Set<Integer> updateRouting(Message message, long controllerId) {
        Node node = resolveAddress(MessageUtil.address(message.getHeader()));
        if (node.getId() == controllerId) {
            node.setStatus(NodeStatus.Controller);
        } else {
            node.setStatus(NodeStatus.Node);
        }
        node.setLastUpdated(System.currentTimeMillis());
        node.getRetx().putAll(MessageUtil.retx(message.getData()));
        node = nodeRepository.saveAndFlush(node);

        Set<Integer> current = node.getRouting();
        Set<Integer> calculated = runFloydWarshall(node);
        Set<Integer> updates = new HashSet<>();
        updates.addAll(calculated.stream().filter(i -> !current.contains(i)).toList());
        updates.addAll(current.stream().filter(i -> !calculated.contains(i)).map(i -> i | MessageUtil.DELETE_BIT).toList());
        return updates;
    }



    Set<Integer> runFloydWarshall(Node nodeToUpdate) {
        var all = liveNodes();
        var controllers = all.stream().filter(n -> n.getStatus() == NodeStatus.Controller).toList();

        if (controllers.isEmpty()) {
            logger.warn("no controller found");
            return Set.of();
        }

        Map<Integer, Node> byAddress = new HashMap<>();
        all.forEach(node -> byAddress.put(node.getAddress(), node));

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
                    Node v = byAddress.get(key);
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
        calculatedRouting.addAll(nodeToUpdate.getUplinkRouting().stream().map(Node::getAddress).collect(Collectors.toSet()));
        calculatedRouting.addAll(nodeToUpdate.getDownlinkRouting().stream().map(Node::getAddress).map(i -> i | MessageUtil.DOWNLINK_BIT).collect(Collectors.toSet()));
        return calculatedRouting;
    }

    private double distance(Node u, Node v) {
        return u.getDistance().getOrDefault(v, Double.MAX_VALUE);
    }
}
