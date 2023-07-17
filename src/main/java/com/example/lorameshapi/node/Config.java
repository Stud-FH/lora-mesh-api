package com.example.lorameshapi.node;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Config {

    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column
    private String channelCode;

}
