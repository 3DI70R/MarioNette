--- Environment for Super Mario Bros 1 game
--- Based on Sethbling's MarI/O

local env = {}

--- Settings -------------

local viewportRadius = 6 -- BoxRadius
local timeoutDuration = 20

--- Forward declarations -------------

local getEnemies,
isEnemyNearby,
isTileSolid

local getTotalFitness,
updateRunState,
getEstimatedTimeoutFramesLeft,
isPlayerDead,
isRunFinished,
isTimeOut,
getTotalFitness

--- Local variables

local marioX, marioXMax, marioY, marioState
local screenX, screenCurrent, screenNext
local worldCurrent, worldNext
local timeLeft

--- Utility functions -------------

function getEnemies()
    local enemies = {}
    for slot = 0, 4 do
        local enemy = memory.readbyte(0xF + slot)
        if enemy ~= 0 then
            local ex = memory.readbyte(0x6E + slot) * 0x100 + memory.readbyte(0x87 + slot)
            local ey = memory.readbyte(0xCF + slot) + 24
            enemies[#enemies + 1] = { x = ex, y = ey }
        end
    end

    return enemies
end

function isTileSolid(dx, dy)
    local x = marioX + dx + 8
    local y = marioY + dy

    local page = math.floor(x / 256) % 2
    local subx = math.floor((x % 256) / 16)
    local suby = math.floor((y - 32) / 16)
    local addr = 0x500 + page * 13 * 16 + suby * 16 + subx

    if suby >= 13 or suby < 0 then
        return false
    end

    return memory.readbyte(addr) ~= 0
end

function isEnemyNearby(enemies, dx, dy)
    for i = 1, #enemies do
        local enemy = enemies[i]
        local distx = math.abs(enemy.x - (marioX + dx))
        local disty = math.abs(enemy.y - (marioY + dy + 16))

        if distx <= 8 and disty <= 8 then
            return true
        end
    end

    return false
end

function updateRunState()
    timeLeft = timeLeft - 1

    if marioX > marioXMax then
        marioXMax = marioX
        timeLeft = timeoutDuration
    end
end

function getEstimatedTimeoutFramesLeft(currentFrame)
    local time = timeLeft
    local frame = currentFrame
    local count = 0

    -- TODO better algorithm without loop

    while true do
        time = time - 1
        frame = frame + 1

        if time + frame / 4 <= 0 then
            return count
        else
            count = count + 1
        end
    end
end

function isPlayerDead()
    return marioState == 0x0B or -- Dying
            marioState == 0x06 or -- Dead
            marioY > 0xFF -- Below viewport (in pit)
end

function isRunFinished(currentFrame)
    return isTimeOut(currentFrame) or isPlayerDead()
end

function isTimeOut(currentFrame)
    return timeLeft + currentFrame / 4 <= 0
end

function getTotalFitness(currentFrame)
    local framesPenalty = 0

    if isPlayerDead() then
        framesPenalty = getEstimatedTimeoutFramesLeft(currentFrame)

        if framesPenalty < 0 then
            framesPenalty = 0
        end
    end

    local fitness = marioXMax - (currentFrame + framesPenalty) / 2

    if marioXMax > 3186 then
        fitness = fitness + 1000
    end

    return fitness
end

--- Environment definition ---------------

env.name = "Mario"
env.game = "Super Mario Bros 1"
env.version = "1.0.0"
env.neuralInputCount = (viewportRadius * 2 + 1) * (viewportRadius * 2 + 1)
env.neuralOutputCount = 6

function env.getInputs()
    local inputs = {}
    local enemies = getEnemies()
    local index = 0

    for dy = -viewportRadius * 16, viewportRadius * 16, 16 do
        for dx = -viewportRadius * 16, viewportRadius * 16, 16 do
            index = index + 1
            if isEnemyNearby(enemies, dx, dy) then
                inputs[index] = -1
            elseif isTileSolid(dx, dy) then
                inputs[index] = 1
            else
                inputs[index] = 0
            end
        end
    end

    return inputs
end

function env.mapOutputs(outputs)
    local input = {}

    input["left"] = outputs[1] > 0
    input["right"] = outputs[2] > 0
    input["up"] = outputs[3] > 0
    input["down"] = outputs[4] > 0
    input["A"] = outputs[5] > 0
    input["B"] = outputs[6] > 0
    input["select"] = false
    input["start"] = false

    joypad.set(1, input)
end

function env.onSimulationReset()
    marioXMax = 0
    timeLeft = timeoutDuration
end

function env.onNewFrame(frame)
    marioX = memory.readbyte(0x6D) * 0x100 + memory.readbyte(0x86)
    marioY = memory.readbyte(0x03B8) + (memory.readbyte(0xB5) - 1) * 0xFF
    marioState = memory.readbyte(0x0E)

    screenX = memory.readbyte(0x03AD)
    screenCurrent = memory.readbyte(0x071A)
    screenNext = memory.readbyte(0x071B)

    worldCurrent = memory.readbyte(0x075F)
    worldNext = memory.readbyte(0x0760)

    updateRunState()
end

function env.getEvaluationState(currentFrame)
    local result = {}

    result.playerX = marioX
    result.playerY = marioY

    result.fitness = getTotalFitness(currentFrame)
    result.isFinished = isRunFinished(currentFrame)

    return result
end

return env