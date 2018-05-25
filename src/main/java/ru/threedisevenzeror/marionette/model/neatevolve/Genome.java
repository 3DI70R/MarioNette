package ru.threedisevenzeror.marionette.model.neatevolve;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;
import ru.threedisevenzeror.marionette.utils.StreamUtils;

import java.util.ArrayList;
import java.util.List;

public class Genome implements NetworkObject {

    public static class Link {

        public int from;
        public int to;
        public float weight;

        public Link(int from, int to, float weight) {
            this.from = from;
            this.to = to;
            this.weight = weight;
        }
    }

    public long id;
    public String description = "Unnamed network";
    public int neuronCount;
    public int inputCount;
    public int outputCount;
    public List<Link> links = new ArrayList<>();

    @Override
    public void write(ByteBuf output) {
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
    public void read(ByteBuf input) {
        id = input.readLong();
        description = StreamUtils.readNullTerminatedString(input);
        neuronCount = input.readInt();
        inputCount = input.readInt();
        outputCount = input.readInt();

        int linkCount = input.readInt();
        links = new ArrayList<>();

        for(int i = 0; i < linkCount; i++) {
            links.add(new Link(input.readInt(),
                    input.readInt(),
                    input.readFloat()));
        }
    }
}
