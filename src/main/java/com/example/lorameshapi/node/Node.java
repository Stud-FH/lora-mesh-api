package com.example.lorameshapi.node;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
@Entity
public class Node {

    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Column
    private int address;

    @Enumerated(EnumType.STRING)
    private NodeStatus status;

    @Column
    private long lastUpdated;

    @ElementCollection
    private Map<Integer, Double> retx = new HashMap<>();

    @Column
    private int sendingCounter = 0;

    @Column
    private int nextReceivingCounter = 0;

    @ElementCollection
    private Set<Integer> routing = new HashSet<>();

    @ElementCollection
    private Set<Integer> missingMessages = new HashSet<>();

    @ElementCollection
    private List<String> statusKeys = new ArrayList<>();

    @Transient
    private transient Map<Node, Double> distance;

    @Transient
    private transient Map<Node, Node> trace;

    @Transient
    private transient Set<Node> uplinkRouting;

    @Transient
    private transient Set<Node> downlinkRouting;
}
