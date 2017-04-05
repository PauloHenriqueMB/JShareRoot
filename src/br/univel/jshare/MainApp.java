package br.univel.jshare;

import java.io.IOException;

import br.univel.jshare.view.ViewMainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * This archive start the application
 * @author hbmat
 * @version 1.0.0
 * @since 31/03/2017
 */
public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	
	/**
	 * Start application
	 */
	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("JShare");
		initRootLayout();
		loadViewMain();
	}
	
	/**
	 * Load rootLayout
	 */
	public void initRootLayout(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();
			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		}catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load view main
	 */
	public void loadViewMain(){
		try{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/ViewMain.fxml"));
			AnchorPane view = (AnchorPane) loader.load();
			rootLayout.setCenter(view);
			ViewMainController controller = loader.getController();
			controller.setMainApp(this);
		}catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Return main stage
	 * @return
	 */
	public Stage getPrimaryStage() {
		return primaryStage;
	}
	
	
	public static void main(String[] args) {
		launch(args);
	}
}
