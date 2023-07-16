package com.example.lorameshapi.log;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/log")
public class LogController {

    private final LogService logService;

    @GetMapping("/q")
    List<LogEntry> query() {
        return logService.query();
    }

    @PostMapping
    void log(@RequestBody LogEntry data) {
        logService.log(data);
    }
}
