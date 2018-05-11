local neural = {}

local function activationSigmoid(x)
    return 2 / (1 + math.exp(-4.9 * x)) - 1
end

function neural.createNetworkDefinition(generation, species, genome, neuronCount)
    local networkInfo = {}

    networkInfo.generation = generation
    networkInfo.species = species
    networkInfo.genome = genome
    networkInfo.neuronCount = neuronCount
    networkInfo.links = {}

    return networkInfo
end

function neural.createLinkDefinition(from, to, weight)
    local link = {}

    link.from = from
    link.to = to
    link.weight = weight

    return link
end

function neural.createNeuron(value)
    local neuron = {}

    neuron.index = 0
    neuron.type = "hidden"
    neuron.value = value
    neuron.links = {}

    function neuron.linkTo(self, otherNeuron, weight)
        local link = {}
        link.neuron = otherNeuron
        link.weight = weight
        table.insert(self.links, link)
        return link
    end

    return neuron
end

function neural.createNetwork(networkInfo)
    local network = {}

    network.bias = createNeuron(1)
    network.inputs = {}
    network.outputs = {}
    network.neurons = {}
    network.updateQueue = {}

    -- TODO: create network from network info

    return network
end

function neural.evaluateNetwork(network)
    for i, n in ipairs(network.updateQueue) do
        if #n.links > 0 then
            local value = 0

            for l in ipairs(n.links) do
                value = value + l.weight * l.neuron.value
            end

            n.value = activationSigmoid(value)
        end
    end
end

return neural