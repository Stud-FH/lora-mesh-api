package com.example.lorameshapi.data;

import com.example.lorameshapi.node.NodeService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

@AllArgsConstructor
@Service
public class DataService {

    private final Logger logger = LoggerFactory.getLogger(DataService.class);

    private final DataRepository dataRepository;
    private final NodeService nodeService;

    private final DateFormat df;

    public void persist(Message message) {
        File file = new File("/data/data");
        if (!file.exists() && !file.mkdirs()) {
            logger.warn("could not create directory " + file);
        }
        var nodeId = MessageUtil.nodeId(message.getHeader());
        var counter = MessageUtil.counter(message.getHeader());
        var node =nodeService.resolveNodeId(nodeId);
        String key = String.format("%s-%s-%d", node.getId(), df.format(new Date()), message.getHeader());

        try {
            Path path = Path.of("/data/data/"+key+".txt");
            Files.write(path, message.getData());
            Data data = new Data();
            data.setHeader(message.getHeader());
            data.setNodeId(nodeId);
            data.setCounter(counter);
            this.dataRepository.save(data);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public List<Data> query() {
        // todo add query params
        return dataRepository.findAll();
    }
}
