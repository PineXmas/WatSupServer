package application;

/**
 * Store info for a room chat message.
 * @author pinex
 *
 */
public class ChatMessage {
	public String content;
	public ChatClient sender;
	
	public ChatMessage(String content, ChatClient sender) {
		this.content = content;
		this.sender = sender;
	}
	
	public String toString() {
		return sender.userName + " ::: " + content;
	}
}
