package com.example.lorameshapi.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Message {
    private int header;
    private byte[] data;
}
