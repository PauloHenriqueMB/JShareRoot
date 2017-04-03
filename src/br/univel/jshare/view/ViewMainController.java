package br.univel.jshare.view;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import br.univel.jshare.MainApp;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.controller.GenerateDialogController;
import br.univel.jshare.controller.ServerController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ViewMainController implements Observer{

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
	
	//Client data
	private InetAddress address;
	private Cliente client = new Cliente();
	
	public ViewMainController() {
	}
	
	@FXML
	public void initialize(){
		disableButtons();
		loadClientData();
	}
	
	public void loadClientData(){
		client.setId(0);
		client.setPorta(3000);
		client.setNome("SRVIEIRA");	
		try {
			client.setIp(InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
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

		if(!serverStatus){
			serverStatus = true;
			serverController.createServer(client);
			handleServer.setText("Desligar");
			System.out.println(client.getIp());
			logServer.appendText("Servidor iniciado "+dateFormat.format(date)+"\n");
		}else{
			serverStatus = false;
			serverController.closeServer();
			handleServer.setText("Ligar");
			logServer.appendText("Servidor desligado "+dateFormat.format(date)+"\n");
		}

	}
	
	/**
	 * Method to estabilish connection with server
	 */
	@FXML
	public void handleConnection(){

		if(fieldIp.getText().length() >= 6 && fieldPort.getLength() == 4){
			try {
				serverController.registrarCliente(client);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}else{
			new GenerateDialogController()
				.generateDialog(AlertType.INFORMATION, 
						"Ip e/ou porta incorreta", "", "O ip e/ou a porta informada estao incorretos");
		}
	}
	
	/*
	 * This method set mainApp variable to controller
	 */
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@Override
	public void update(Observable o, Object arg) {
		
	}

}
