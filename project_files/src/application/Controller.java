package application;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class Controller {
	/***********************************************
	 * UI CONTROLS
	 ***********************************************/
	
	public Button btnStart;
	public Button btnStop;
	public Button btnRoomList;
	public Button btnRoomMessages;
	public Button btnUserList;
	public Button btnSysMessageHistory;
	public TextArea txtDisplay;
	public TextField txtRoomName;
	public TextField txtPortNumber;
	
	/***********************************************
	 * SYSTEM DATA
	 ***********************************************/
	
	WSServer server;
	
	// TODO used isInit to make sure the init function is called once. Improve this later
	boolean isInit = false;

	/***********************************************
	 * FUNCTIONS
	 ***********************************************/
	
	/**
	 * Initialize environment for WatSup server
	 */
	void init() {		
		//get port number
		int portNumber = WSSettings._DEFAULT_PORT;
		try {
			portNumber = Integer.valueOf(txtPortNumber.getText());
		} catch (NumberFormatException e) {
			ErrandBoy.printlnError(e, "Invalid port number");
		}
		
		//create server object
		server = new WSServer(portNumber, WSSettings._MAX_ROOMS, WSSettings._MAX_USERS);
	}
	
	/***********************************************
	 * CONTROL HANDLERS
	 ***********************************************/
	
	public void onBtnStart_Click(ActionEvent event) {
		ErrandBoy.println("btnStart is pressed");
		
		init();
		
		server.Start();
		btnStart.setDisable(true);
		btnStop.setDisable(false);
	}
	
	public void onBtnStop_Click(ActionEvent event) {
		ErrandBoy.println("btnStop is pressed");
		
		server.Stop();
		server = null;
		
		btnStart.setDisable(false);
		btnStop.setDisable(true);
	}
	
	public void onBtnRoomList_Click(ActionEvent event) {
		ErrandBoy.println("btnRoomList is pressed");
	}
	
	public void onBtnRoomMessages_Click(ActionEvent event) {
		ErrandBoy.println("btnRoomMessages is pressed");
	}
	
	public void onBtnUserList_Click(ActionEvent event) {
		ErrandBoy.println("btnUserList is pressed");
	}
	
	public void onBtnSysMessageHistory_Click(ActionEvent event) {
		ErrandBoy.println("btnSysMessageHistory is pressed");
	}
}
