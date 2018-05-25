package ru.threedisevenzeror.marionette.network;

import ru.threedisevenzeror.marionette.model.message.NetworkObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Message protocol, used for message class <-> id mapping
 */
public class MessageMapping {

    private Map<Integer, Class<NetworkObject>> packetClassMap;
    private Map<Class<NetworkObject>, Integer> classPacketMap;
    private Map<Integer, Supplier<NetworkObject>> packetFactoryMap;

    public MessageMapping() {
        packetClassMap = new HashMap<>();
        classPacketMap = new HashMap<>();
        packetFactoryMap = new HashMap<>();
    }

    public <T extends NetworkObject> void registerPacket(int id, Class<T> packetClass, Supplier<T> packetFactory) {
        packetClassMap.put(id, (Class<NetworkObject>) packetClass);
        classPacketMap.put((Class<NetworkObject>) packetClass, id);
        packetFactoryMap.put(id, (Supplier<NetworkObject>) packetFactory);
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

    public NetworkObject createObjectForPacket(int id) {
        Supplier<NetworkObject> supplier = packetFactoryMap.get(id);

        if(supplier == null) {
            throw new IllegalArgumentException("Unknown packet id: " + id);
        }

        return supplier.get();
    }

    public Class<NetworkObject> getPacketClass(int id) {
        return packetClassMap.get(id);
    }
}
