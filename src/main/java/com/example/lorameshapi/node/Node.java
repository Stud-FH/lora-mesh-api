package com.example.lorameshapi.node;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Entity
public class Node {

    @Id
    @Column(name = "serial_id", nullable = false)
    private Long serialId;

    @Column
    private int nodeId = -1;

    @Enumerated(EnumType.STRING)
    private NodeStatus status = NodeStatus.Down;

    @Column
    private long lastUpdated = System.currentTimeMillis();

    @ElementCollection
    private Map<Integer, Double> retx = new HashMap<>();

    @Column
    private int sendingCounter = 0;

    @Column
    private int nextReceivingCounter = 0;

    @ElementCollection
    private Set<Integer> missingMessages = new HashSet<>();

    @Column(length = 8192)
    private String ipa;
}
