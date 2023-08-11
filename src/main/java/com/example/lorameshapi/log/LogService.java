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

    public void log(long sid, LogEntry data) {
        LogEvent event = new LogEvent();
        event.setSid(sid);
        event.setTimestamp(System.currentTimeMillis());
        event.setSeverity(data.getSeverity());
        event.setModuleInfo(data.getModuleInfo());
        StringBuilder sb = new StringBuilder();
        sb.append(data.getModuleInfo())
                .append("\n")
                .append(df.format(new Date()))
                .append("\n")
                .append(new String(data.getData()));
        try {
            event = logEventRepository.save(event);
            var dir = Path.of("/data/log/"+sid);
            var dirFile = dir.toFile();
            if (!dirFile.exists() && !dirFile.mkdirs()) {
                throw new RuntimeException("could not create directory " + dir);
            }
            Files.write(dir.resolve(String.format("%d.%s.txt", event.getId(), data.getSeverity())), sb.toString().getBytes());
        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    public List<LogEvent> query() {
        // todo add query params
        return logEventRepository.findAll();
    }
}
