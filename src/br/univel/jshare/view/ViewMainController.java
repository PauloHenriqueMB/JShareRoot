package br.univel.jshare.view;

import br.univel.jshare.MainApp;
import br.univel.jshare.controller.ServerController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ViewMainController {

	private MainApp mainApp;
	@FXML
	private TextField fieldIp;
	@FXML
	private TextField fieldPort;
	@FXML
	private TextField fieldAssunto;
	@FXML
	private Button handleCon;
	@FXML
	private Button btnBuscar;
	@FXML
	private Button handleServer;
	@FXML
	private Button btnLimpar;
	@FXML
	private ComboBox<?> filtro;
	@FXML
	private TextArea logServer;
	
	private boolean serverStatus = false;
	private ServerController serverController = new ServerController();
	
	public ViewMainController() {
	}
	
	@FXML
	public void initialize(){
		disableButtons();
	}
	
	@FXML
	public void disableButtons(){
		btnLimpar.setDisable(true);
		btnBuscar.setDisable(true);
		fieldAssunto.setDisable(true);
		filtro.setDisable(true);
	}
	
	@FXML
	public void handleServer(){
		if(!serverStatus){
			serverStatus = true;
			serverController.createServer();
			handleServer.setText("Desligar");
		}else{
			serverStatus = false;
			serverController.closeServer();
			handleServer.setText("Ligar");
		}
	}
	
	/*
	 * This method set mainApp variable to controller
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
	
}
