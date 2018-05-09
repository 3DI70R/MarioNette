package ru.threedisevenzeror.marionette.network;

import java.util.HashMap;
import java.util.Map;

/**
 * Message protocol, used for message class <-> id mapping
 */
public class MessageMapping {

    private Map<Integer, Class<?>> packetClassMap;
    private Map<Class<?>, Integer> classPacketMap;

    public MessageMapping() {
        packetClassMap = new HashMap<>();
        classPacketMap = new HashMap<>();
    }

    public void registerPacket(int id, Class<?> packetClass) {

        if(id > 0xff) {
            throw new IllegalArgumentException("Current protocol version uses 1 byte for packet ids, " +
                    "use id lower than 255");
        }

        packetClassMap.put(id, packetClass);
        classPacketMap.put(packetClass, id);
    }

    public int getPacketId(Class<?> clazz) {
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Integer id = classPacketMap.get(clazz);

            if(id != null) {
                return id;
            } else {
                currentClass = currentClass.getSuperclass();
            }
        }

        throw new IllegalArgumentException("Unknown packet class: " + clazz.getName());
    }

    public Class<?> getPacketClass(int id) {
        return packetClassMap.get(id);
    }
}
