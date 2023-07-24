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

    @PostMapping("/in")
    public Collection<Integer> post(
            @RequestBody int header
    ) {
        return nodeService.registerAndListLosses(header);
    }

    @GetMapping("/out/{nodeId}")
    public int get(
            @PathVariable int nodeId
    ) {
        return nodeService.getCorrespondenceCounter(nodeId, false);
    }

    @PostMapping("/out/{nodeId}")
    public int getAndIncrement(
            @PathVariable int nodeId
    ) {
        return nodeService.getCorrespondenceCounter(nodeId, true);
    }
}
