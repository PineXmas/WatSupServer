package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
		start_02(primaryStage);
	}
	
	public void start_01(Stage primaryStage) {
		try {
			BorderPane root = new BorderPane();
			Scene scene = new Scene(root,400,400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void start_02(Stage primaryStage) {
		try {
			Parent root = FXMLLoader.load( getClass().getResource("ServerDebugUI.fxml"));
			Scene scene = new Scene(root);
			
			//add styles to the scene
			//scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			//add scene to stage
			primaryStage.setScene(scene);
			
			//display stage
			primaryStage.setTitle("WatSup Server Viewer");
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
		
//		WSMessage.main();
		
	}
}
