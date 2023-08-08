package com.example.lorameshapi.data;

import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    public static int nodeId(int header) {
        return header & 63;
    }

    public static int counter(int header) {
        return header & 7936;
    }

    public static boolean isJoin(int header) {
        return (header & 57472) == 24576;
    }

    public static boolean isRetx(int header) {
        return (header & 57472) == 16384;
    }

    public static Map<Integer, Double> retx(byte[] data) {
        Map<Integer, Double> retx = new HashMap<>();
        for (int i = 0; i + 1 < data.length; i+=2) {
            retx.put((int) data[i], 256.0 / data[i+1]);
        }
        return retx;
    }
}
