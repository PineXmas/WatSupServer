package application;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
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
	boolean isRunInConsole = false;

	/***********************************************
	 * FUNCTIONS
	 ***********************************************/

	@FXML
	protected void initialize() {
	}

	/**
	 * Initialize environment for WatSup server using the given port number
	 */
	void init(int portNumber) {

		// create server object
		server = new WSServer(portNumber, WSSettings._MAX_ROOMS, WSSettings._MAX_USERS);
	}

	/**
	 * Initialize environment for WatSup server
	 */
	void init() {
		// get port number
		int portNumber = WSSettings._DEFAULT_PORT;
		try {
			portNumber = Integer.valueOf(txtPortNumber.getText());
		} catch (Exception e) {
			ErrandBoy.printlnError(e, "Error while parsing port number, use default port " + WSSettings._DEFAULT_PORT);
			portNumber = WSSettings._DEFAULT_PORT;
		}

		// create server object
		init(portNumber);
	}

	/***********************************************
	 * CONTROL HANDLERS
	 ***********************************************/

	public void onBtnStart_Click(ActionEvent event) {
		if (isRunInConsole && event instanceof WSActionEvent) {
			init(((WSActionEvent) event).portNumber);
			server.start();
			return;
		}

		ErrandBoy.println("btnStart is pressed");

		init();
		server.start();

		btnStart.setDisable(true);
		btnStop.setDisable(false);
	}

	public void onBtnStop_Click(ActionEvent event) {
		if (isRunInConsole) {
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

			return;
		}

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

	public static void main(String[] args) {

		try {
			InputStreamReader inputStreamReader = new InputStreamReader(System.in);
			BufferedReader reader = new BufferedReader(inputStreamReader);
			String command;
			boolean isQuit = false;

			/**
			 * CONSOLE or GUI
			 */
			while (true) {
				ErrandBoy.println("Select C(onsole) or G(UI): ");
				command = reader.readLine().toUpperCase().trim();
				
				if (command.equals("G")) {
					Main.main(args);
					return;
				}
				
				if (command.equals("C")) {
					break;
				}
			}

			Controller controller = new Controller();
			controller.isRunInConsole = true;
			boolean isRunning = false;
			ErrandBoy.println("Please enter:\n" + "    START: to start server\n" + "    STOP : to stop server\n"
					+ "    QUIT : to exit this program\n");

			isQuit = false;
			while (!isQuit) {
				command = reader.readLine().toUpperCase().trim();

				switch (command) {
				case "START":
					if (isRunning) {
						ErrandBoy.println("Server is already running.");
						break;
					}

					int port;
					try {
						ErrandBoy.println("Enter a port number: ");
						port = Integer.valueOf(reader.readLine());
					} catch (Exception e) {
						ErrandBoy.printlnError(e,
								"Error while parsing port number, use default port " + WSSettings._DEFAULT_PORT);
						port = WSSettings._DEFAULT_PORT;
					}

					ErrandBoy.println("Starting server...");
					isRunning = true;

					WSActionEvent event = new WSActionEvent(port);
					controller.onBtnStart_Click(event);
					break;

				case "STOP":
					if (!isRunning) {
						ErrandBoy.println("Server is not running.");
						break;
					}

					ErrandBoy.println("Stopping server...");
					isRunning = false;
					controller.onBtnStop_Click(null);
					break;
				case "QUIT":
					if (isRunning) {
						ErrandBoy.println("Server is still running, use STOP first!");
						break;
					}

					isQuit = true;
					break;
				default:
					break;
				}
			}

			reader.close();

			ErrandBoy.println("Server shut down. Bye!");
		} catch (Exception e) {
			ErrandBoy.printlnError(e, "Error while reading user's command");
		}
	}
}
