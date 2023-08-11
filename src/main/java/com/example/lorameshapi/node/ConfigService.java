package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
@Service
public class ConfigService {

    private final Logger logger = LoggerFactory.getLogger(ConfigService.class);

    private final NodeRepository nodeRepository;
    private final NodeService nodeService;

    private final DateFormat df;

    public String getChannel() {
        try {
            return Files.readString(Path.of("/data/channel.txt"));
        } catch (Exception e) {
            logger.error(e.toString());
            return "error";
        }
    }

    public byte[] status(long id, byte[] data) {
        File file = new File("/data/node-status");
        if (!file.exists() && !file.mkdirs()) {
            logger.warn("could not create directory " + file);
        }
        Node entity = nodeService.getById(id);
        String key = String.format("%d-%s", id, df.format(new Date()));
        try {
            Path path = Path.of("/data/node-status/"+key+".txt");
            Files.write(path, data);
            entity.getStatusKeys().add(key);
            nodeRepository.save(entity);
        } catch (Exception e) {
            logger.error(e.toString());
        }

        try {
            return Files.readAllBytes(Path.of(String.format("/data/config/%d.txt", id)));
        } catch (Exception e) {
            logger.error(e.toString());
            return new byte[]{};
        }
    }

    public byte[] getJar(long lm) {
        try {
            var path = Path.of("/data/node.jar");
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            long lastModified = attr.lastModifiedTime().to(TimeUnit.MILLISECONDS);
            if (lm >= lastModified) return new byte[]{};
            return Files.readAllBytes(path);
        } catch (Exception e) {
            logger.error(e.toString());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
