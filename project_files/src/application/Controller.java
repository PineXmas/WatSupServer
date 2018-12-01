package application;

import java.util.ArrayList;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
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
	public Button btnUserList;
	public Button btnKickUser;
	public TextArea txtDisplay;
	public TextField txtUserName;
	public TextField txtPortNumber;

	/***********************************************
	 * SYSTEM DATA
	 ***********************************************/

	WSServer server;

	/***********************************************
	 * FUNCTIONS
	 ***********************************************/

	@FXML
	protected void initialize() {
	}

	/**
	 * Initialize environment for WatSup server
	 */
	void init() {
		// get port number
		int portNumber = WSSettings._DEFAULT_PORT;
		try {
			portNumber = Integer.valueOf(txtPortNumber.getText());
		} catch (NumberFormatException e) {
			ErrandBoy.printlnError(e, "Invalid port number");
		}

		// create server object
		server = new WSServer(portNumber, WSSettings._MAX_ROOMS, WSSettings._MAX_USERS);
	}

	/***********************************************
	 * CONTROL HANDLERS
	 ***********************************************/

	public void onBtnStart_Click(ActionEvent event) {
		ErrandBoy.println("btnStart is pressed");

		init();

		server.start();
		btnStart.setDisable(true);
		btnStop.setDisable(false);
	}

	public void onBtnStop_Click(ActionEvent event) {
		ErrandBoy.println("btnStop is pressed");

		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				// send all users KICKED_OUT message
				for (int i = 0; i < server.listClientSockets.size(); i++) {
					if (server.listClientSockets.get(i).userName != null) {
						server.listClientSockets.get(i).enqueueMessage(new WSMError(WSMCode.ERR_KICKED_OUT));
					}
				}

				try {
					ErrandBoy.println("Sleep 1 sec to let all the sender-threads to send KICKED_OUT msg");
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					ErrandBoy.printlnError(e, "Error while sleeping before kick out all users");
				}

				server.stop();
				server = null;

				btnStart.setDisable(false);

			}
		});

		btnStart.setDisable(true);
		btnStop.setDisable(true);
	}

	public void onBtnRoomList_Click(ActionEvent event) {
		ErrandBoy.println("btnRoomList is pressed");
		txtDisplay.clear();
		
		if (server == null) {
			return;
		}
		
		txtDisplay.appendText("[ROOM LIST]\n\n");
		for (ChatRoom room : server.listRooms) {
			txtDisplay.appendText(room.roomName + "\n");
		}
	}

	public void onBtnUserList_Click(ActionEvent event) {
		ErrandBoy.println("btnUserList is pressed");
		txtDisplay.clear();
		
		if (server == null) {
			return;
		}
		
		txtDisplay.appendText("[USER LIST]\n\n");
		for (WSClientHandler client : server.listClientSockets) {
			txtDisplay.appendText(client.getName() + "\n");
		}
	}

	public void onBtnKickUser_Click(ActionEvent event) {
		if (server == null) {
			return;
		}

		String userName = txtUserName.getText();
		int found = server.searchUser(userName);
		if (found >= 0) {
			// send the user KICKED_OUT message
			server.listClientSockets.get(found).enqueueMessage(new WSMError(WSMCode.ERR_KICKED_OUT));

			try {
				ErrandBoy.println("Sleep 1 sec to let the sender-thread of " + userName + " to send KICKED_OUT msg");
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				ErrandBoy.printlnError(e, "Error while sleeping before kick out user " + userName);
			}

			// remove user out of WatSup
			ArrayList<ChatRoom> listJoinedRooms = server.removeUser(userName);

			// notify all remaining users
			server.sendAllUsers(server.genListUsersAllMsg());

			// notify other users in the rooms this user has joined
			for (ChatRoom chatRoom : listJoinedRooms) {
				WSMListUsersResp msgNewUserList = chatRoom.genListUsersRespMsg();
				server.sendAllUsers(msgNewUserList);
			}
		}
	}
}
