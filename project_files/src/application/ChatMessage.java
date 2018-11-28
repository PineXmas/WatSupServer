package application;

/**
 * Store info for a room chat message.
 * @author pinex
 *
 */
public class ChatMessage {
	public String content;
	public WSClientHandler sender;
	
	public ChatMessage(String content, WSClientHandler sender) {
		this.content = content;
		this.sender = sender;
	}
	
	public String toString() {
		return sender.getName() + " ::: " + content;
	}
}
