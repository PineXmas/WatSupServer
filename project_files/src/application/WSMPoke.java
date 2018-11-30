package application;

/**
 * This class serves as a signal to stop the receiver-thread in the server side
 * @author pinex
 *
 */
public class WSMPoke extends WSMessage {

	public WSMPoke() {
	}

	@Override
	public void parse2Attributes() {
	}

	@Override
	public String toString() {
		return "WSMPoke";
	}

}
