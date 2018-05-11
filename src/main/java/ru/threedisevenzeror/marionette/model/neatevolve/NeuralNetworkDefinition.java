package ru.threedisevenzeror.marionette.model.neatevolve;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

import java.util.ArrayList;
import java.util.List;

public class NeuralNetworkDefinition implements KryoSerializable {

    public static class Link {
        public int from;
        public int to;
        public float weight;
    }

    public long id;
    public String description = "Unnamed network";
    public int neuronCount;
    public int inputCount;
    public int outputCount;
    public List<Link> links = new ArrayList<>();

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeLong(id);
        StreamUtils.writeNullTerminatedString(output, description);
        output.writeInt(neuronCount);
        output.writeInt(inputCount);
        output.writeInt(outputCount);
        output.writeInt(links.size());

        for (Link l : links) {
            output.writeInt(l.from);
            output.writeInt(l.to);
            output.writeFloat(l.weight);
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        id = input.readLong();
        description = StreamUtils.readNullTerminatedString(input);
        neuronCount = input.readInt();
        inputCount = input.readInt();
        outputCount = input.readInt();

        int linkCount = input.readInt();
        links = new ArrayList<>();

        for(int i = 0; i < linkCount; i++) {
            Link l = new Link();
            l.from = input.readInt();
            l.to = input.readInt();
            l.weight = input.readFloat();
        }
    }
}
