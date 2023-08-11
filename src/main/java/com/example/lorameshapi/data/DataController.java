package com.example.lorameshapi.data;

import com.example.lorameshapi.node.NodeService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/data")
public class DataController {

    private final DataService dataService;
    private final NodeService nodeService;

    @GetMapping
    public boolean ping() {
        return true;
    }

    @PostMapping()
    public Collection<Integer> feed(@RequestBody Message message) {
        dataService.persist(message);
        return nodeService.registerAndListLosses(message.getHeader());
    }

    @GetMapping("/q")
    public List<Data> query() {
        return dataService.query();
    }
}
