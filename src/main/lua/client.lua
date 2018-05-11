local socket = require("socket")
local struct = require("struct")
local neural = require("neural")

local client = {}

--- Forward definition of module's locals

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
sendClientParamsMessage,
sendMemoryDump

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
local emulationEvaluationPeriod = 500
local emulationSpeed = "normal"
local debuggingInfo = true
local eventHandler = emptyHandler

--- Messaging utils ---------------------------

local currentMessageBufferId = 0x00
local currentMessageBuffer = {}
local connectionMimic = {} -- hacky way to disguise input connection as string
                           -- So struct packing library can work with connection

function connectionMimic.setConnection(self, c)
    self.c = c
end

function connectionMimic.len(self)
    return 1000000000
end

function connectionMimic.sub(self, from, to)
    local len = to - from + 1
    return self.c:receive(len)
end

function readMessage(c, format)
    connectionMimic:setConnection(c)
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

function sendMessage(c)
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

    c:send(lengthPrefix)
    c:send(resultMessage)
    currentMessageBuffer = {}
end

function client.closeConnection(c, reason)
    sendConnectionClosedMessage(c, reason)
    c:close()
    emu.print("Closing connection: " .. reason)

    if eventHandler.onDisconnected then
        eventHandler.onDisconnected(reason)
    end
end

function printSeparator()
    emu.print("---------------------------")
end

--- Input message handlers -------------

function pingPongMessageHandler(c)
    local sendPong = readMessage(c, "b")

    if sendPong then
        sendPingPongMessage(c)
    end
end

function setSettingsMessageHandler(c)
    local emulationSpeedData,
    socketTimeoutData,
    emulationEvaluationPeriodData,
    debuggingInfoData = readMessage(c, ">biib")

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

function showMessageMessageHandler(c)
    local message, duration = readMessage(c, ">si")
    emu.message(message)

    if debuggingInfo then
        emu.print("Display message: \"" .. message .. "\" for " .. tostring(duration) .. " milliseconds")
    end
end

function evaluateNetworkMessageHandler(c)
    local networkCount = readMessage(c, ">i")
    local networks = {}

    for i = 1, networkCount do
        local id,
        description,
        neuronCount,
        inputCount,
        outputCount,
        linkCount = readMessage(c, ">lsiiii")

        local network = neural.createNetworkDescription(id, description, neuronCount, inputCount, outputCount)

        for j = 1, linkCount do
            local from, to, weight = readMessage(c, ">iif")
            network:addLink(from, to, weight)
        end

        networks[i] = network
    end

    eventHandler.onNewNetworksAdded(networks)
end

function connectionClosedMessageHandler(c)
    local reason = readMessage(c, ">s")
    client.closeConnection(c, reason)
    eventHandler.onDisconnected(reason)
end

--- Message senders -------------------

function sendPingPongMessage(c)
    beginMessage(sendPingPongMessage)
    writeMessage("b", 0)
    sendMessage(c)
end

function sendConnectionClosedMessage(c, reason)
    beginMessage(sendConnectionClosedMessage)
    writeMessage(">s", reason)
    sendMessage(c)
end

function sendClientParamsMessage(c)
    beginMessage(sendClientParamsMessage)
    writeMessage(">sss",
            "prototype", -- protocol version
            "FCEUX client", -- client name
            "fceux" -- client type
    )
    sendMessage(c)
end

function sendMemoryDump(c)
    beginMessage(sendMemoryDump)
    writeRawMessage(memory.readbyterange(0, 0x800))
    sendMessage(c)
end

--- Packet -> Handler mapping ----------

packetTable =
{
    [0x00] = setSettingsMessageHandler,
    [0x01] = showMessageMessageHandler,

    [sendMemoryDump] = 0x02,

    [0x03] = evaluateNetworkMessageHandler,

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

        local connection = socket.connect(host, port)

        if connection then

            emu.print("Successfully connected to: " .. addr)

            emu.registerexit(function()
                client.closeConnection(connection, "Script evaluation on emulator was stopped")
            end)

            sendClientParamsMessage(connection)
            startNetworkPacketHandlerLoop(connection)
        else
            emu.print("Cannot establish connection to " .. host .. ":" .. port .. ", retrying in 10 seconds...")
            runForTime(10000, emu.frameadvance)
        end
    end
end

function startNetworkPacketHandlerLoop(c)
    while true do
        c:settimeout(0)
        local packetHeader, error = c:receive(5)

        if packetHeader then
            handlePacket(c, packetHeader)
        else
            if error == "closed" then
                break
            else
                runForTime(emulationEvaluationPeriod, eventHandler.onFrameSimulation)
            end
        end
    end
end

function handlePacket(c, packetHeader)
    local packetSize, packetType = struct.unpack(">iB", packetHeader)
    local handler = packetTable[packetType]

    if debuggingInfo then
        local hexType = string.format("%x", packetType)
        local messageType = packetNames[packetType]
        printSeparator()
        emu.print("Received message [0x" .. hexType .. ", " .. messageType .. "] with size " .. packetSize)
    end

    if handler then
        c:settimeout(socketTimeout)
        handler(c)
    else
        client.closeConnection(c, "Unknown message received (" .. packetType .. ")")
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