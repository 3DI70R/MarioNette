package ru.threedisevenzeror.marionette.model.packets.fceux;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoSerializable;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import ru.threedisevenzeror.marionette.model.neatevolve.NeuralNetworkDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Packet which holds information about neural networks that FCEUX should evaluate
 */
public class FceuxEvaluateNetwork implements KryoSerializable {

    /**
     * List with neural network definitions
     */
    public List<NeuralNetworkDefinition> networks = new ArrayList<>();

    public FceuxEvaluateNetwork() {
    }

    public FceuxEvaluateNetwork(NeuralNetworkDefinition... networks) {
        this.networks = Arrays.asList(networks);
    }

    @Override
    public void write(Kryo kryo, Output output) {
        output.writeInt(networks.size());

        for (NeuralNetworkDefinition d : networks) {
            d.write(kryo, output);
        }
    }

    @Override
    public void read(Kryo kryo, Input input) {
        int networkCount = input.readInt();
        networks = new ArrayList<>(networkCount);

        for(int i = 0; i < networkCount; i++) {
            NeuralNetworkDefinition d = new NeuralNetworkDefinition();
            d.read(kryo, input);
            networks.add(d);
        }
    }
}
