package application;

/**
 * This class serves as a signal to stop the receiver-thread in the server side
 * @author pinex
 *
 */
public class WSMStopSerer extends WSMessage {

	public WSMStopSerer(int opcode, int dataLength, byte[] data) {
		super(opcode, dataLength, data);
	}

	@Override
	public void parse2Attributes() {
	}

	@Override
	public String toString() {
		return "WSMStopServer";
	}

}
