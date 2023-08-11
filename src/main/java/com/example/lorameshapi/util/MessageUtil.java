package com.example.lorameshapi.util;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class MessageUtil {

    // TODO add rest
    public static final int ADDRESS_MASK = 63;
    public static final int COUNTER_MASK = 7936;
    public static final int TYPE_MASK = 57344;
    public static final int COUNTER_SHIFT = 8;
    public static final int ADDRESS_LIMIT = 63;
    public static final int COUNTER_LIMIT = 32;
    public static final int DOWNLINK_BIT = 128;
    public static final int DELETE_BIT = 64;

    public static JoinData joinData(byte[] data) {
        ByteBuffer buf = ByteBuffer.wrap(data);
        double mediatorRetx = buf.get() / 256.0;
        long id = buf.getLong();
        return new JoinData(mediatorRetx, id);
    }

    public static int address(int header) {
        return header & ADDRESS_MASK;
    }

    public static int counter(int header) {
        return (header & COUNTER_MASK) >>> COUNTER_SHIFT;
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
