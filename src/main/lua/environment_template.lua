--- Work in progress environment template ---------------

environmentName = "Test environment"
environmentVersion = "1.0.0"
neuralNetworkInputCount = 64
neuralNetworkOutputCount = 8

--- This function should return "Inputs" of neural network, values between -1 and 1
--- These inputs will be feeded to neural network to calculate outputs
function getInputs()

end

--- This function should map outputs of neural network, to actual actions
--- either this is button presses, or more complicated logic
function mapOutputs(neuralNetworkOutput)

end

--- Function which should return information about current run
--- so its results can be tracked by algorithm
function getEvaluationState()
    local result = {}

    result.xPlayer = 0 -- Player X position in current location
    result.yPlayer = 0 -- Player Y position in current location
    result.playerAnimation = 0 -- Player animation frame index
    result.location = 0 -- Player's location

    result.fitness = 0 -- Current evaluation fitness
    result.isFinished = false -- Is evaluation finished

    return result
end

--- Callback function which called when new simulation is started
--- You should reset your variables here, so each run starts with exact same conditions
function onNewSimulation()

end

--- Callback function which called every frame
--- You can do here frame-dependent logic, count frames, calculate scores, update variables, whatever
function onNewFrame()

end




