package com.example.lorameshapi.data;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@AllArgsConstructor
@Service
public class DataService {

    private final DataRepository dataRepository;

    public void persist(Data data) {
        this.dataRepository.save(data);
    }

    public List<Data> query() {
        // todo add query params
        return dataRepository.findAll();
    }
}
