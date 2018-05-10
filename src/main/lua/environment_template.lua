--- Work in progress environment template ---------------

local env = {}

env.name = "Test environment"
env.game = "Some game"
env.version = "1.0.0"
env.inputCount = 64
env.outputCount = 6

--- This function should return "Inputs" of neural network, values between -1 and 1
--- These inputs will be feeded to neural network to calculate outputs
--- This method should return array table with length of "inputCount"
function env.getInputs()

end

--- This function should map outputs of neural network, to actual actions
--- either this is button presses, or more complicated logic
--- This method should return array table with length of "inputCount"
function env.mapOutputs(neuralNetworkOutputs)

end

--- Function which should return information about current run
--- so its results can be tracked by algorithm
function env.getEvaluationState(currentFrame)
    local result = {}

    result.xPlayer = 0 -- Player X position in current location
    result.yPlayer = 0 -- Player Y position in current location
    result.playerAnimation = 0 -- Player animation frame index
    result.location = 0 -- Player's location

    result.fitness = 0 -- Current evaluation fitness
    result.isFinished = false -- Is evaluation finished

    return result
end

--- Callback function which called when new simulation is resetted
--- You should reset your variables here, so each run starts with exact same conditions
function env.onSimulationReset()

end

--- Callback function which called every frame
--- You can do here frame-dependent logic, count frames, calculate scores, update variables, whatever
function env.onNewFrame(currentFrame)

end

--- Callback function which called when GUI is drawn
--- You can draw custom gui here
function env.onGui()

end




