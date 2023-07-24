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
    List<LogEvent> query() {
        return logService.query();
    }

    @PostMapping("/{id}")
    void log(@PathVariable long id, @RequestBody LogEntry data) {
        logService.log(id, data);
    }
}
