local neural = {}

local function activationSigmoid(x)
    return 2 / (1 + math.exp(-4.9 * x)) - 1
end

function neural.createNetworkDescription(id, description, neuronCount, inputCount, outputCount)
    local networkInfo = {}

    networkInfo.id = id
    networkInfo.description = description
    networkInfo.neuronCount = neuronCount
    networkInfo.inputCount = inputCount
    networkInfo.outputCount = outputCount
    networkInfo.links = {}

    function networkInfo.addLink(self, from, to, weight)
        local link = {}

        link.from = from
        link.to = to
        link.weight = weight

        table.insert(self.links, link)
    end

    return networkInfo
end

local function createNeuron(value)
    local neuron = {}

    neuron.index = 0
    neuron.type = "hidden"
    neuron.value = value
    neuron.activation = activationSigmoid
    neuron.links = {}

    function neuron.linkTo(self, otherNeuron, weight)
        local link = {}

        link.neuron = otherNeuron
        link.weight = weight

        table.insert(self.links, link)
        return link
    end

    function neuron.update(self)
        local valueSum = 0

        for i, l in ipairs(self.links) do
            valueSum = valueSum + l.weight * l.neuron.value
        end

        self.value = self.activation(valueSum)
    end

    return neuron
end

function neural.createNetwork(networkInfo)
    local network = {}

    network.id = networkInfo.id
    network.generation = networkInfo.generation
    network.species = networkInfo.species
    network.genome = networkInfo.genome

    network.bias = nil
    network.inputs = {}
    network.outputs = {}
    network.neurons = {}
    network.updateQueue = {}

    for i = 1, networkInfo.neuronCount do
        local neuron = createNeuron(0)
        neuron.index = i

        network.neurons[i] = neuron
    end

    for i = 1, networkInfo.inputCount do
        local inputNeuron = network.neurons[i]
        inputNeuron.type = "input"
        network.inputs[i] = inputNeuron
    end

    local outputStartIndex = #network.neurons - networkInfo.outputCount
    for i = 1, networkInfo.outputCount do
        local outputNeuron = network.neurons[outputStartIndex + i]
        outputNeuron.type = "output"
        network.outputs[i] = outputNeuron
    end

    local biasIndex = networkInfo.inputCount
    local bias = network.neurons[biasIndex]
    bias.value = 1
    bias.type = "bias"
    network.bias = bias

    for i, link in pairs(networkInfo.links) do
        local from = network.neurons[link.from + 1]
        local to = network.neurons[link.to + 1]

        from:linkTo(to, link.weight)
    end

    for i, n in ipairs(network.neurons) do
        if #n.links > 0 then
            table.insert(network.updateQueue, n)
        end
    end

    function network.clear(self)
        for i, n in pairs(self.neurons) do
            n.value = 0
        end

        self.neurons[biasIndex].value = 1
    end

    function network.evaluate(self, inputs)
        local inputCount = math.min(#self.inputs, #inputs)
        local outputCount = #self.outputs
        local output = {}

        for i = 1, inputCount do
            self.inputs[i].value = inputs[i]
        end

        for i, n in ipairs(self.updateQueue) do
            n:update()
        end

        for i = 1, outputCount do
            output[i] = self.outputs[i].value
        end

        return output
    end

    return network
end

return neural