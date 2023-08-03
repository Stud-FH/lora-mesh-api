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
        if (!new File("/data/data").mkdirs()) {
            logger.warn("could not create directories");
        }
        var node =nodeService.resolveNodeId(MessageUtil.nodeId(message.getHeader()));
        String key = String.format("%s-%s-%d", node.getId(), df.format(new Date()), message.getHeader());

        try {
            Path path = Path.of("/data/data/"+key+".txt");
            Files.write(path, message.getData());
            Data data = new Data();
            data.setId(key);
            data.setHeader(message.getHeader());
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
