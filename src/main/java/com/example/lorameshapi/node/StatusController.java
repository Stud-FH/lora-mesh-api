package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController
@RequestMapping("/status")
public class StatusController {

    private final NodeService nodeService;

    @GetMapping
    public String helloWorld() {
        return "hello world";
    }

    @PostMapping
    public String status(@RequestParam long serialId, @RequestBody String ipa) {
        return nodeService.status(serialId, ipa);
    }
}
