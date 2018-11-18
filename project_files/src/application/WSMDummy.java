package application;

public class WSMDummy extends WSMessage {
	private String rawText;
	
	public WSMDummy(int opcode, int dataLength, byte[] data) {
		super(opcode, dataLength, data);
	}

	String getDataAsText() {
		return rawText;
	}

	@Override
	public void parseToAttributes() {
		rawText = new String(data);
	}

}
