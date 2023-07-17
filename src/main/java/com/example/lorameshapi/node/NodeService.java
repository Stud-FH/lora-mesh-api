package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@AllArgsConstructor
@Service
public class NodeService {

    private static final int counterLimit = 32;

    private final NodeRepository nodeRepository;

    private Node findByNodeId(int nodeId) {
        return nodeRepository.findByNodeId(nodeId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "node id " + nodeId + " not found"));
    }

    public void put(Node node) {
        Node entity = nodeRepository.findBySerialId(node.getSerialId()).orElseGet(Node::new);
        if (entity.getSerialId() == null) entity.setSerialId(node.getSerialId());
        entity.setStatus(node.getStatus());
        entity.setLastUpdated(System.currentTimeMillis());
        entity.getRetx().replaceAll((k,v) -> 0.0);
        entity.getRetx().putAll(node.getRetx());
        nodeRepository.save(entity);
    }

    public void feed(Node data) {
        Node entity = findByNodeId(data.getNodeId());
        entity.setStatus(NodeStatus.Node);
        entity.setLastUpdated(System.currentTimeMillis());
        entity.getRetx().putAll(data.getRetx());
        nodeRepository.save(entity);
    }

    public void putRetx(int nodeId, int otherId, double retx) {
        Node entity = findByNodeId(nodeId);
        entity.getRetx().put(otherId, retx);
        nodeRepository.save(entity);
    }

    public int allocateNodeId(long serialId) {
        Node entity = nodeRepository.findById(serialId).orElseGet(Node::new);
        if (entity.getSerialId() == null) entity.setSerialId(serialId);
        if (entity.getNodeId() > 0) return entity.getNodeId();
        int nodeId = 1;
        while (nodeRepository.existsByNodeId(nodeId)) nodeId++;
        if (nodeId > 63) throw new IllegalStateException("node ids exhausted");
        entity.setNodeId(nodeId);
        entity.setStatus(NodeStatus.Joining);
        entity.setLastUpdated(System.currentTimeMillis());
        nodeRepository.save(entity);
        return nodeId;
    }

    public int getCorrespondenceCounter(int nodeId, boolean increment) {
        var entity = findByNodeId(nodeId);
        int counter = entity.getSendingCounter();
        if (increment) {
            entity.setSendingCounter((counter + 1) % counterLimit);
            nodeRepository.save(entity);
        }
        return counter;
    }

    public Collection<Integer> registerAndListLosses(int nodeId, int counter) {
        var entity = findByNodeId(nodeId);

        if (counter == entity.getNextReceivingCounter()) {
            entity.setNextReceivingCounter((counter + 1) % counterLimit);
        } else if(entity.getMissingMessages().contains(counter)) {
            entity.getMissingMessages().remove(counter);
            nodeRepository.save(entity);
            return new HashSet<>();
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

    public String status(long serialId, String ipa) {
        Node entity = nodeRepository.findById(serialId).orElseGet(Node::new);
        entity.setIpa(ipa);
        entity = nodeRepository.save(entity);
        return entity.getStatus().toString();
    }
}
