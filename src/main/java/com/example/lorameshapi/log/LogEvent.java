package com.example.lorameshapi.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LogEvent {

    @Id
    @Column(name="id", nullable = false)
    private String id;

    @Column
    private long timestamp;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column
    private String moduleInfo;

}
