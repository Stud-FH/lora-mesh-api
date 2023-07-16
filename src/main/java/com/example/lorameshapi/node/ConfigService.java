package com.example.lorameshapi.node;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class ConfigService {

    private final ConfigRepository configRepository;

    public Config get() {
        return configRepository.findById(1).orElseGet(() -> configRepository.save(this.defaultConfig()));
    }

    private Config defaultConfig() {
        Config config = new Config();
        config.setId(1);
        config.setFrequency(20);
        config.setDataRate(20);
        config.setSpreadingFactor(20);
        return config;
    }
}
