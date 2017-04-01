package br.univel.jshare.view;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.univel.jshare.MainApp;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
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
	
	private Date date;
	private boolean serverStatus = false;
	private Map<Cliente, List<Arquivo>> mapaClientes = new HashMap<>();
	private ServerController serverController = new ServerController();
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	public ViewMainController() {
	}
	
	@FXML
	public void initialize(){
		disableButtons();
	}
	
	/**
	 * Method to dissable limpar,buscar,assunto e filtro
	 */
	@FXML
	public void disableButtons(){
		btnLimpar.setDisable(true);
		btnBuscar.setDisable(true);
		fieldAssunto.setDisable(true);
		filtro.setDisable(true);
	}
	
	/**
	 * Method to turn on/off server
	 */
	@FXML
	public void handleServer(){
		
		date = new Date();
		mapaClientes = serverController.getMapaClientes();
		
		if(!serverStatus){
			serverStatus = true;
			serverController.createServer();
			handleServer.setText("Desligar");
			logServer.appendText("Servidor iniciado "+dateFormat.format(date)+"\n");
		}else{
			serverStatus = false;
			serverController.closeServer();
			handleServer.setText("Ligar");
			logServer.appendText("Servidor desligado "+dateFormat.format(date)+"\n");
		}
		
		logServer.appendText("Ips conectados na lista: \n");
		mapaClientes.forEach((k, v)->{
			logServer.appendText(k.getIp());
		});

	}
	
	/*
	 * This method set mainApp variable to controller
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
	
}
