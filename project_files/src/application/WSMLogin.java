package application;

import java.net.Socket;
import java.util.Arrays;

public class WSMLogin extends WSMessage {

	public String userName;
	
	public WSMLogin(int opcode, int dataLength, byte[] data, Socket sender) {
		super(opcode, dataLength, data, sender);
	}
	
	public WSMLogin(String userName) {
		super(WSMCode.OPCODE_LOGIN.rawCode, userName.length(), null);
		this.userName = userName;
	}

	@Override
	public void parse2Attributes() {
		userName = parse2String(Arrays.copyOfRange(data, 8, data.length - 8));
	}

	@Override
	public String toString() {
		
		return opcode + "|" +  dataLength + "|" + userName;
	}

}
