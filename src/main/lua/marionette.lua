socket = require("socket")
struct = require("struct")
require("protocol")

function evaluateFrame()
	emu.frameadvance()
end

local connection = socket.connect("localhost", 34710)

sendClientInfoPacket(connection)
sendWaitingForNewCommandsPacket(connection)

processNetworkMessages(connection)