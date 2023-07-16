package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;

@AllArgsConstructor
@RestController
@RequestMapping("/correspondence")
public class CorrespondenceController {

    private final NodeService nodeService;

    @GetMapping("/in")
    public Collection<Integer> post(
            @RequestParam int nodeId,
            @RequestParam int counter
    ) {
        return nodeService.registerAndListLosses(nodeId, counter);
    }

    @GetMapping("/out")
    public int get(
            @RequestParam int nodeId,
            @RequestParam Optional<Boolean> increment
    ) {
        return nodeService.getCorrespondenceCounter(nodeId, increment.orElse(false));
    }
}
