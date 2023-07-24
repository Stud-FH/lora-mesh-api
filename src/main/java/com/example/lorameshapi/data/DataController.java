package com.example.lorameshapi.data;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/data")
public class DataController {

    private final DataService dataService;

    @GetMapping
    public boolean ping() {
        return true;
    }

    @PostMapping()
    public void feed(@RequestBody Message message) {
        dataService.persist(message);
    }

    @GetMapping("/q")
    public List<Data> query() {
        return dataService.query();
    }
}
