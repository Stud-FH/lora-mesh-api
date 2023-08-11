package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@AllArgsConstructor
@RestController
@RequestMapping("/correspondence")
public class CorrespondenceController {

    private final NodeService nodeService;

    @PostMapping("/in")
    public Collection<Integer> post(
            @RequestBody int header
    ) {
        return nodeService.registerAndListLosses(header);
    }

    @GetMapping("/out/{address}")
    public int get(
            @PathVariable int address
    ) {
        return nodeService.getCorrespondenceCounter(address, false);
    }

    @PostMapping("/out/{address}")
    public int getAndIncrement(
            @PathVariable int address
    ) {
        return nodeService.getCorrespondenceCounter(address, true);
    }
}
