package com.example.lorameshapi.node;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface NodeRepository extends JpaRepository<Node, Long> {

    boolean existsByAddress(int address);
    Optional<Node> findByAddress(int address);
    Collection<Node> findAllByLastUpdatedGreaterThanEqual(long lastUpdated);
}
