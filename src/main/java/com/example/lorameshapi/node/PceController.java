package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/pce")
public class PceController {
    
    private final ConfigService configService;
    private final NodeService nodeService;

    @PostMapping
    public String heartbeat(@RequestBody Node node) {
        nodeService.put(node);
        return configService.get().getChannelCode();
    }

    @PostMapping("/node-id")
    public int allocateNodeId(
            @RequestParam int serialId,
            @RequestParam int mediatorId,
            @RequestParam double mediatorRetx
    ) {
        var result = nodeService.allocateNodeId(serialId);
        if (mediatorId != -1) {
            nodeService.putRetx(mediatorId, result, mediatorRetx);
        }
        return result;
    }

    @PostMapping("/feed")
    public List<String> feed(
            @RequestParam int controllerSerialId,
            @RequestBody Node data) {
        nodeService.feed(data);
        // todo generate commands
        return new ArrayList<>();
    }

    @GetMapping("/q")
    public List<Node> query() {
        return nodeService.query();
    }
}
