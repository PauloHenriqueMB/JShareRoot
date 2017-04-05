package br.univel.jshare.view;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javafx.stage.Stage;

public class ViewMainController{

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
	private Stage stage;
	private boolean clientStatus = false;
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
		disableButtons(true);
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
	public void disableButtons(boolean status){
		btnLimpar.setDisable(status);
		btnBuscar.setDisable(status);
		fieldAssunto.setDisable(status);
		filtro.setDisable(status);
	}
	
	/**
	 * Method to turn on/off server
	 */
	@FXML
	public void handleServer(){
		
		date = new Date();

		if(!serverStatus){
			serverStatus = true;
			serverController.createServer(client,this);
			handleServer.setText("Desligar");
			System.out.println(client.getIp());
			logServer.appendText("Servidor iniciado "+dateFormat.format(date)+"\n");
		}else{
			System.exit(0);
		}

	}
	
	/**
	 * Method to estabilish connection with server
	 */
	@FXML
	public void handleConnection(){

		String ip = fieldIp.getText();
		int porta = Integer.parseInt(fieldPort.getText());
		
		if(fieldIp.getText().length() >= 6 && fieldPort.getLength() == 4){
			if(!clientStatus){
				handleCon.setText("Desconectar");
				serverController.conectarCliente(client, ip, porta);
				clientStatus = true;
				disableButtons(false);
			}else{
				serverController.desconectarCliente(client);
				handleCon.setText("Conectar");
				clientStatus = false;
				disableButtons(true);
			}			
		}else{
			new GenerateDialogController()
				.generateDialog(AlertType.INFORMATION, 
						"Ip e/ou porta incorreta", "", "O ip e/ou a porta informada estao incorretos");
		}
	}
	
	@FXML
	public void makeSearch(){
		Map<Cliente, List<Arquivo>> newMap = new HashMap<>();
		newMap = serverController.getArchives();
		newMap.forEach((k,v)->{
			v.forEach(e->{
				logServer.appendText(k.getNome()+" possui o arquivo "+e.getNome()+"\n");
			});
		});
	}

	public void setUserConnected(String nome){
		logServer.appendText(nome+" conectou e enviou/atualizou sua lista de arquivo!\n");
	}
	
	public void setUserDisconnected(String nome){
		logServer.appendText(nome+" disconectou do servidor!\n");
	}

	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
