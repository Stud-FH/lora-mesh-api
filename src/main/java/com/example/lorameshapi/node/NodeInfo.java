package com.example.lorameshapi.node;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NodeInfo {
    private long id;
    private int address;
    private NodeStatus status;
    private Map<Integer, Double> retx;
}
