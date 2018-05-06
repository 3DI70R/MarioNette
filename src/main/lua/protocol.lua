socket = require("socket")
struct = require("struct")

-- In ------------------

function evaluateUntillPacketHandler(c)
	local evaluateUntill = struct.unpack(">l", c:receive(8))
	local diff = evaluateUntill - (socket.gettime() * 1000)

	emu.print("Waiting for next command for " .. tostring(math.floor(diff)) .. " miliseconds")

	sendWaitingForNewCommandsPacket(c)

	while socket.gettime() * 1000 < evaluateUntill do
		evaluateFrame()
	end

end

function setEmulatorParamsPacketHandler(c)
	local speedType = struct.unpack("b", c:receive(1))
	local mode = "normal"

	if speedType == 1 then
		mode = "nothrottle"
	elseif speedType == 2 then
		mode = "maximum"
	end

	emu.speedmode(mode)
	emu.print("Speed changed to: " .. mode)
end

function getEvaluationStatePacketHandler(c)
	sendEvaluationStatePacket(c)
end

-- Out -----------------

function sendClientInfoPacket(c)
	writeMessageId(c, 0xff)
	c:send(struct.pack("ss", 
		"FCEUX client", 
		"fceux"))
end

function sendWaitingForNewCommandsPacket(c)
	writeMessageId(c, 0x00)
end

function sendEvaluationStatePacket(c)
	writeMessageId(c, 0x02)
end

-- Utility -------------

function writeMessageId(c, id)
	c:send(struct.pack("b", id))
end

-- Processing ----------

local messageHandlers = 
{
	[0x00] = evaluateUntillPacketHandler, --> sendWaitingForNewCommandsPacket
	[0x01] = setEmulatorParamsPacketHandler,
	[0x02] = getEvaluationStatePacketHandler --> sendEvaluationStatePacket
}

function processNetworkMessages(c)
	while true do
		local messageType = struct.unpack("b", c:receive(1))
		local handler = messageHandlers[messageType]

		emu.print("Received message with ID " .. tostring(messageType))

		if handler then
			handler(c)
		end
	end
end

