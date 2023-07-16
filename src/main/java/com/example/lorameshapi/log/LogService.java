package com.example.lorameshapi.log;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class LogService {

    private final LogEntryRepository logEntryRepository;

    public void log(LogEntry logEntry) {
        logEntryRepository.save(logEntry);
    }

    public List<LogEntry> query() {
        // todo add query params
        return logEntryRepository.findAll();
    }
}
