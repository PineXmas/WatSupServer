package application;

import java.net.Socket;

public class WSMListRooms extends WSMNoData {

	public WSMListRooms(int opcode, int dataLength, byte[] data, WSClientHandler sender) {
		super(opcode, dataLength, data, sender);
	}
	
	public WSMListRooms() {
		super(WSMCode.OPCODE_LIST_ROOMS);
	}

}
