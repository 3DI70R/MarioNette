socket = require("socket")
struct = require("struct")

--- Default settings -------------------

local socketTimeout = 1000
local emulationEvaluationPeriod = 500
local emulationSpeed = "normal"
local debuggingInfo = false

--- Messaging utils ---------------------------

local currentMessageBufferId = 0x00
local currentMessageBuffer = {}

function mimicConnectionAsString(c)
    local result = {}

    function result.len(self)
        return 1000000000
    end

    function result.sub(self, from, to)
        local len = to - from + 1
        return c:receive(len)
    end

    return result
end

function readMessage(c, format)
    return struct.unpack(format, mimicConnectionAsString(c))
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

function closeConnection(c, reason)
    sendConnectionClosedMessage(c, reason)
    c:close()
    emu.print("Closing connection: " .. reason)
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
        emu.print("Display message: \"" .. message .. "\" for " .. tostring(duration) .. " miliseconds")
    end
end

function connectionClosedMessageHandler(c)
    local reason = readMessage(c, ">s")
    closeConnection(c, reason)
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
    [0xfc] = "Connection closed",
    [0xfd] = "Ping",
    [0xfe] = "Handshake"
}

--- Processing -------------------------

function getTimeMilis()
    return socket.gettime() * 1000
end

function startNetworkConnectionLoop(host, port)

    while true do
        printSeparator()

        local addr = host .. ":" .. port
        emu.print("Trying to establish connection with " .. addr .. "...")

        local connection = socket.connect(host, port)

        if connection then

            emu.print("Successfully connected to: " .. addr)

            emu.registerexit(function()
                closeConnection(connection, "Script evaluation on emulator was stopped")
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
                evaluateEmulation(emulationEvaluationPeriod)
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
        closeConnection(c, "Unknown message received (" .. packetType .. ")")
    end
end

function evaluateEmulation(time)
    runForTime(time, function()
        emu.frameadvance()
        -- TODO: Actual neatevolve loop logic
    end)
end

function runForTime(time, func)
    local evaluateUntill = getTimeMilis() + time
    local runOnce = false

    while not runOnce or getTimeMilis() < evaluateUntill do
        func()
        runOnce = true
    end
end

startNetworkConnectionLoop("localhost", 34710)