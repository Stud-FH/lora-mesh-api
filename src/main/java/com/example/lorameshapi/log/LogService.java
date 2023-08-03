package com.example.lorameshapi.log;

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
public class LogService {

    private final Logger logger = LoggerFactory.getLogger(LogService.class);

    private final DateFormat df;

    private final LogEventRepository logEventRepository;

    public void log(long id, LogEntry data) {
        if (!new File("/data/log").mkdirs()) {
            logger.warn("could not create directories");
        }
        String key = String.format("%d-%s-%s", id, data.getSeverity(), df.format(new Date()));
        LogEvent event = new LogEvent();
        event.setId(key);
        event.setTimestamp(System.currentTimeMillis());
        event.setSeverity(data.getSeverity());
        event.setModuleInfo(data.getModuleInfo());
        try {
            Path path = Path.of("/data/log/"+key+".txt");
            Files.write(path, data.getData());
            logEventRepository.save(event);
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public List<LogEvent> query() {
        // todo add query params
        return logEventRepository.findAll();
    }
}
