package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/status")
public class StatusController {

    private final ConfigService configService;

    @GetMapping
    public byte[] helloWorld(@RequestParam long lm) {
        return configService.getJar(lm);
    }

    @PostMapping("/{id}")
    public byte[] status(@PathVariable long id, @RequestBody byte[] data) {
        return configService.status(id, data);
    }
}
