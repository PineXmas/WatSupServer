package application;

import javafx.event.ActionEvent;

public class WSActionEvent extends ActionEvent{
	public int portNumber;
	
	public WSActionEvent(int portNumber) {
		this.portNumber = portNumber;
	}
}
