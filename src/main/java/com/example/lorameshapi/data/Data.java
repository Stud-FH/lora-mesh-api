package com.example.lorameshapi.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
public class Data {

    @Id
    @GeneratedValue
    @Column(name="id", nullable = false)
    private Long id;

    @Column
    private Date timestamp = new Date();

    @Column
    private int header;

    @Column
    private int nodeId;

    @Column
    private int counter;

}
