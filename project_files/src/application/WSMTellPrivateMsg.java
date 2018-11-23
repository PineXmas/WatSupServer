package application;

import java.net.Socket;

public class WSMTellPrivateMsg extends WSMOneLabelChatMsg {
	public WSMTellPrivateMsg(int opcode, int dataLength, byte[] msgBytes, Socket sender) {
		super(opcode, dataLength, msgBytes, sender);
	}
	
	public WSMTellPrivateMsg(String name, String chatContent) {
		super(WSMCode.OPCODE_TELL_PRIVATE_MSG, name, chatContent);
	}
}
