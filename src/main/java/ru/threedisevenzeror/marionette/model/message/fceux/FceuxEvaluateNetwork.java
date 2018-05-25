package ru.threedisevenzeror.marionette.model.message.fceux;

import io.netty.buffer.ByteBuf;
import ru.threedisevenzeror.marionette.model.message.NetworkObject;
import ru.threedisevenzeror.marionette.model.neatevolve.Genome;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Packet which holds information about neural networks that FCEUX should evaluate
 */
public class FceuxEvaluateNetwork implements NetworkObject {

    /**
     * List with neural network definitions
     */
    public List<Genome> networks = new ArrayList<>();

    public FceuxEvaluateNetwork() {
    }

    public FceuxEvaluateNetwork(Genome... networks) {
        this.networks = Arrays.asList(networks);
    }

    @Override
    public void write(ByteBuf output) {
        output.writeInt(networks.size());

        for (Genome d : networks) {
            d.write(output);
        }
    }

    @Override
    public void read(ByteBuf input) {
        int networkCount = input.readInt();
        networks = new ArrayList<>(networkCount);

        for(int i = 0; i < networkCount; i++) {
            Genome d = new Genome();
            d.read(input);
            networks.add(d);
        }
    }
}
