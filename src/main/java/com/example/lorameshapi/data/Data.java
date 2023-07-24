package com.example.lorameshapi.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Data {

    @Id
    @Column(name="id", nullable = false)
    private String id;

    @Column
    private int header;

}
