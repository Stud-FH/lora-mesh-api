package com.example.lorameshapi.log;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class LogEvent {

    @Id
    @GeneratedValue
    @Column(name="id", nullable = false)
    private Long id;

    @Column(name="sid", nullable = false)
    private Long sid;

    @Column
    private long timestamp;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column
    private String moduleInfo;

}
