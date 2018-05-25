local socket = require("socket")
local struct = require("struct")
local neural = require("neural")

local client = {}

--- Forward definition of module's locals

local connection

local readMessage,
beginMessage,
writeMessage,
writeRawMessage,
sendMessage

local pingPongMessageHandler,
setSettingsMessageHandler,
showMessageMessageHandler,
evaluateNetworkMessageHandler,
connectionClosedMessageHandler

local sendPingPongMessage,
sendConnectionClosedMessage,
sendClientParamsMessage

local packetTable,
packetNames

local getTimeMillis,
startNetworkPacketHandlerLoop,
handlePacket,
runForTime,
printSeparator

local emptyHandler = {
    onDisconnected = function (reason) end;
    onNewNetworksAdded = function (networks) end;
    onFrameSimulation = emu.frameadvance;
}

--- Default settings -------------------

local socketTimeout = 1000
local emulationEvaluationPeriod = 50
local emulationSpeed = "nothrottle"
local debuggingInfo = false
local eventHandler = emptyHandler

--- Messaging utils ---------------------------

local currentMessageBufferId = 0x00
local currentMessageBuffer = {}
local connectionMimic = {} -- hacky way to disguise input connection as string
                           -- So struct packing library can work with connection

function connectionMimic.len(self)
    return 1000000000
end

function connectionMimic.sub(self, from, to)
    local len = to - from + 1
    return connection:receive(len)
end

function readMessage(format)
    return struct.unpack(format, connectionMimic)
end

function beginMessage(func)
    currentMessageBufferId = packetTable[func]
end

function writeMessage(format, ...)
    table.insert(currentMessageBuffer, struct.pack(format, ...))
end

function writeRawMessage(value)
    table.insert(currentMessageBuffer, value)
end

function sendMessage()
    local resultMessage = table.concat(currentMessageBuffer)
    local messageSize = resultMessage:len() + 1
    local lengthPrefix = struct.pack(">iB",
            messageSize,
            currentMessageBufferId)

    if debuggingInfo then
        printSeparator()
        local hexId = string.format("%x", currentMessageBufferId)
        local name = packetNames[currentMessageBufferId]
        emu.print("Sent message [0x" .. hexId .. ", " .. name .. "] with size " .. messageSize)
    end

    connection:send(lengthPrefix)
    connection:send(resultMessage)
    currentMessageBuffer = {}
end

function client.closeConnection(reason)
    sendConnectionClosedMessage(reason)
    connection:close()
    emu.print("Closing connection: " .. reason)

    if eventHandler.onDisconnected then
        eventHandler.onDisconnected(reason)
    end
end

function printSeparator()
    emu.print("---------------------------")
end

--- Input message handlers -------------

function pingPongMessageHandler()
    local sendPong = readMessage("b")

    if sendPong then
        sendPingPongMessage()
    end
end

function setSettingsMessageHandler()
    local emulationSpeedData,
    socketTimeoutData,
    emulationEvaluationPeriodData,
    debuggingInfoData = readMessage(">biib")

    if emulationSpeedData == 1 then
        emulationSpeed = "nothrottle"
    elseif emulationSpeedData == 2 then
        emulationSpeed = "maximum"
    else
        emulationSpeed = "normal"
    end

    debuggingInfo = debuggingInfoData ~= 0
    socketTimeout = socketTimeoutData
    emulationEvaluationPeriod = emulationEvaluationPeriodData

    if debuggingInfo then
        emu.print("Received new settings: ")
        emu.print("Debugging info: " .. tostring(debuggingInfo))
        emu.print("Emulation speed: " .. emulationSpeed)
        emu.print("Read timeout: " .. socketTimeout)
        emu.print("Emulation evaluation period: " .. emulationEvaluationPeriod)
    end

    emu.speedmode(emulationSpeed)
end

function showMessageMessageHandler()
    local message, duration = readMessage(">si")
    emu.message(message)

    if debuggingInfo then
        emu.print("Display message: \"" .. message .. "\" for " .. tostring(duration) .. " milliseconds")
    end
end

function evaluateNetworkMessageHandler()
    local networkCount = readMessage(">i")
    local networks = {}

    for i = 1, networkCount do
        local id,
        description,
        neuronCount,
        inputCount,
        outputCount,
        linkCount = readMessage(">lsiiii")

        local network = neural.createNetworkDescription(id, description, neuronCount, inputCount, outputCount)

        for j = 1, linkCount do
            local from, to, weight = readMessage(">iif")
            network:addLink(from, to, weight)
        end

        networks[i] = network
    end

    eventHandler.onNewNetworksAdded(networks)
end

function connectionClosedMessageHandler()
    local reason = readMessage(">s")
    client.closeConnection(reason)
    eventHandler.onDisconnected(reason)
end

--- Message senders -------------------

function sendPingPongMessage()
    beginMessage(sendPingPongMessage)
    writeMessage("b", 0)
    sendMessage()
end

function sendConnectionClosedMessage(reason)
    beginMessage(sendConnectionClosedMessage)
    writeMessage(">s", reason)
    sendMessage()
end

function sendClientParamsMessage()
    beginMessage(sendClientParamsMessage)
    writeMessage(">sss",
            "prototype", -- protocol version
            "FCEUX client", -- client name
            "fceux" -- client type
    )
    sendMessage()
end

function client.sendMemoryDump()
    beginMessage(client.sendMemoryDump)
    writeRawMessage(memory.readbyterange(0, 0x800))
    sendMessage()
end

function client.sendEvaluationResult(id, fitness)
    beginMessage(client.sendEvaluationResult)
    writeMessage(">lf", id, fitness)
    sendMessage()
end

--- Packet -> Handler mapping ----------

packetTable =
{
    [0x00] = setSettingsMessageHandler,

    [0x01] = showMessageMessageHandler,

    [client.sendMemoryDump] = 0x02,

    [0x03] = evaluateNetworkMessageHandler,

    [client.sendEvaluationResult] = 0x04,

    [0xfc] = connectionClosedMessageHandler,
    [sendConnectionClosedMessage] = 0xfc,

    [0xfd] = pingPongMessageHandler,
    [sendPingPongMessage] = 0xfd,

    [sendClientParamsMessage] = 0xfe
}

packetNames =
{
    [0x00] = "Set Settings",
    [0x01] = "Show message",
    [0x02] = "Send memory dump",
    [0x03] = "Evaluate network",
    [0x04] = "Evaluation result",
    [0xfc] = "Connection closed",
    [0xfd] = "Ping",
    [0xfe] = "Handshake"
}

--- Processing -------------------------

function getTimeMillis()
    return socket.gettime() * 1000
end

function client.setCallbackHandlers(handler)
    eventHandler = handler
end

function client.startNetworkConnectionLoop(host, port)
    while true do
        printSeparator()

        local addr = host .. ":" .. port
        emu.print("Trying to establish connection with " .. addr .. "...")

        connection = socket.connect(host, port)

        if connection then

            emu.print("Successfully connected to: " .. addr)

            emu.registerexit(function()
                client.closeConnection("Script evaluation on emulator was stopped")
            end)

            sendClientParamsMessage()
            startNetworkPacketHandlerLoop()
        else
            emu.print("Cannot establish connection to " .. host .. ":" .. port .. ", retrying in 10 seconds...")
            runForTime(10000, emu.frameadvance)
        end
    end
end

function startNetworkPacketHandlerLoop()
    while true do
        connection:settimeout(0)
        local packetHeader, error = connection:receive(5)

        if packetHeader then
            handlePacket(packetHeader)
        else
            if error == "closed" then
                break
            else
                runForTime(emulationEvaluationPeriod, eventHandler.onFrameSimulation)
            end
        end
    end
end

function handlePacket(packetHeader)
    local packetSize, packetType = struct.unpack(">iB", packetHeader)
    local handler = packetTable[packetType]

    if debuggingInfo then
        local hexType = string.format("%x", packetType)
        local messageType = packetNames[packetType]
        printSeparator()
        emu.print("Received message [0x" .. hexType .. ", " .. messageType .. "] with size " .. packetSize)
    end

    if handler then
        connection:settimeout(socketTimeout)
        handler()
    else
        client.closeConnection("Unknown message received (" .. packetType .. ")")
    end
end

function runForTime(time, func)
    local evaluateUntill = getTimeMillis() + time
    local runOnce = false

    while not runOnce or getTimeMillis() < evaluateUntill do
        func()
        runOnce = true
    end
end

return client