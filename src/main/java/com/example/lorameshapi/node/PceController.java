package com.example.lorameshapi.node;

import com.example.lorameshapi.data.Message;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/pce")
public class PceController {
    
    private final ConfigService configService;
    private final NodeService nodeService;

    @PostMapping
    public String heartbeat(@RequestBody NodeInfo data) {
        nodeService.put(data);
        return configService.getChannel();
    }

    @PostMapping("/address")
    public int allocateAddress(
            @RequestParam int mediatorId,
            @RequestParam double mediatorRetx,
            @RequestBody long id
    ) {
        var result = nodeService.allocateAddress(id);
        if (mediatorId != -1) {
            nodeService.putRetx(mediatorId, result, mediatorRetx);
        }
        return result;
    }

    @PostMapping("/feed")
    public List<String> feed(
            @RequestBody Message data,
            @RequestParam long controllerId) {
        return nodeService.feed(data, controllerId);
    }

    @GetMapping("/q")
    public List<Node> query() {
        return nodeService.query();
    }
}
