package com.example.lorameshapi.log;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogEntry {
    private Severity severity;
    private String moduleInfo;
    private byte[] data;
}
