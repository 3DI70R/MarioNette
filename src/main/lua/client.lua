socket = require("socket")
struct = require("struct")

--- Default settings -------------------

local socketTimeout = 1000
local emulationEvaluationPeriod = 500
local emulationSpeed = "normal"
local debuggingInfo = false

--- IO Utils ---------------------------

function wrapConnectionAsStream(c)
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

function writeData(c, format, ...)
    return c:send(struct.pack(format, ...))
end

function readData(c, format)
    return struct.unpack(format, wrapConnectionAsStream(c))
end

function writePacketId(c, func)
    return writeData(c, ">b", outPacketTable[func])
end

function closeConnection(c, reason)
    sendDisconnectedPacket(c, reason)
    c:close()
end

--- Input message handlers -------------

function pingPacketHandler(c)
    sendPongPacket(c)
end

function settingsPacketHandler(c)
    local emulationSpeedData,
    socketTimeoutData,
    emulationEvaluationPeriodData,
    debuggingInfoData = readData(c, ">biib")

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

function showMessagePacketHandler(c)
    local message, duration = readData(c, ">si")
    emu.message(message)

    if debuggingInfo then
        emu.print("Display message: " .. message .. " for " .. tostring(duration) .. " miliseconds")
    end
end

--- Output packet senders --------------

function sendPongPacket(c)
    writePacketId(c, sendPongPacket)
end

function sendDisconnectedPacket(c, reason)
    writePacketId(c, sendDisconnectedPacket)
    writeData(c, ">s", reason)
end

function sendClientParamsPacket(c)
    writePacketId(c, sendClientParamsPacket)
    writeData(c, ">bss",
            0x01,           -- protocol version
            "FCEUX client",     -- client name
            "fceux")            -- client type
end

--- Packet -> Handler mapping ----------

inPacketTable =
{
    [0x00] = pingPacketHandler,
    [0x01] = settingsPacketHandler,
    [0x02] = showMessagePacketHandler
}

outPacketTable =
{
    [sendPongPacket] = 0x00,
    [sendDisconnectedPacket] = 0x01,

    [sendClientParamsPacket] = 0xff
}

--- Processing -------------------------

function getTimeMilis()
    return socket.gettime() * 1000
end

function startNetworkConnectionLoop(host, port)

    while true do
        local connection = socket.connect("localhost", 34710)

        if connection then

            emu.registerexit(function()
                closeConnection(connection, "Script evaluation on emulator was stopped")
            end)

            sendClientParamsPacket(connection)
            startNetworkPacketHandlerLoop(connection)
        else
            emu.print("Cannot establish connection to " .. host .. ":" .. port ", retrying...")
            emu.frameadvance()
        end
    end
end

function startNetworkPacketHandlerLoop(c)

    emu.print("Successfully connected to: " .. c:getsockname())

    while true do
        c:settimeout(0)
        local packetId = c:receive(1)

        if packetId then
            handlePacket(c, packetId)
        else
            evaluateEmulation(emulationEvaluationPeriod)
        end
    end
end

function handlePacket(c, packetId)
    local packetType = struct.unpack("b", packetId)
    local handler = inPacketTable[packetType]

    if debuggingInfo then
        emu.print("Received packet with id: " .. packetType)
    end

    if handler then
        c:settimeout(socketTimeout)
        handler(c)
    else
        emu.print("Unknown packet received: " .. packetType .. ", terminating connection")
        closeConnection(c, "Unknown packet received")
    end
end

function evaluateEmulation(time)
    local evaluateUntill = getTimeMilis() + time
    local runOnce = false

    while not runOnce or getTimeMilis() < evaluateUntill do
        emu.frameadvance()
        runOnce = true
        -- TODO: Actual neural loop logic
    end
end

startNetworkConnectionLoop("localhost", 34710)