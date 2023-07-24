package com.example.lorameshapi.node;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class NodeInfo {
    private long serialId;
    private int nodeId;
    private NodeStatus status;
    private Map<Integer, Double> retx;
}
