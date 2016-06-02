package application;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;


public class TimedAlarmMain extends Application {
	
	@Override
	public void start(Stage primaryStage) {
		
		try {
			
			GridPane root = (GridPane) FXMLLoader.load(getClass().getResource("AppLayout.fxml"));
			
			Scene scene = new Scene(root, 400, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.getIcons().add(new Image("file:res/icon.png"));
			primaryStage.setTitle("Time-O-Matic");
			primaryStage.setScene(scene);
			primaryStage.sizeToScene();
			primaryStage.show();
			
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
