package application;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {
	public Button btnStart;
	public Button btnStop;
	public Button btnRoomList;
	public Button btnRoomMessages;
	public Button btnUserList;
	public Button btnSysMessageHistory;
	public TextArea txtDisplay;
	public TextField txtRoomName;
	
	public void onBtnStart_Click(ActionEvent event) {
		System.out.println("btnStart is pressed");
	}
}
