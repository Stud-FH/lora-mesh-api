package com.example.lorameshapi.log;

import com.example.lorameshapi.node.NodeStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LogEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column
    private long timestamp;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column
    private String text;

    @Column
    private Long nodeSerialId;

    @Column
    private int nodeId;

    @Enumerated(EnumType.STRING)
    private NodeStatus nodeStatus;

}
