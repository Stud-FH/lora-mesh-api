package com.example.lorameshapi.node;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {

    boolean existsByNodeId(int nodeId);
    Optional<Node> findByNodeId(int nodeId);
    Optional<Node> findBySerialId(long serialId);
}