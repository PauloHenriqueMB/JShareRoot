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
import br.univel.jshare.comum.TipoFiltro;
import br.univel.jshare.controller.GenerateDialogController;
import br.univel.jshare.controller.ServerController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

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
	private ComboBox<TipoFiltro> filtro;
	@FXML
	private TextArea logServer;
	@FXML
	private TableView<Arquivo> tableFiles;
	@FXML
	private TableColumn<Cliente, String> colAutor = new TableColumn<>("autor");
	@FXML
	private TableColumn<Arquivo,String> colArquivo = new TableColumn<>("arquivo");
	@FXML
	private TableColumn<Arquivo, String> colExtensao = new TableColumn<>("extensao");
	@FXML
	private TableColumn<Arquivo, Date> colData = new TableColumn<>("data");
	@FXML
	private TableColumn<Arquivo, Long> colTamanho = new TableColumn<>("tamanho");
	@FXML
	private TextField valorFiltro;
	
	private Date date;
	private boolean clientStatus = false;
	private boolean serverStatus = false;
	private ServerController serverController = new ServerController();
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private ObservableMap<Cliente,List<Arquivo>> mapaObserver = FXCollections.observableHashMap();
	
	//Client data
	private Cliente client = new Cliente();
	
	public ViewMainController() {
	}
	
	@FXML
	public void initialize(){
		
		loadClientData();
		disableButtons(true);
		
		filtro.getItems().add(TipoFiltro.EXTENSAO);
		filtro.getItems().add(TipoFiltro.TAMANHO_MAX);
		filtro.getItems().add(TipoFiltro.TAMANHO_MIN);
		
		colArquivo.setCellValueFactory(new PropertyValueFactory<>("nome"));
		
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
		valorFiltro.setDisable(status);
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
		newMap = serverController.getArchives(fieldAssunto.getText(),filtro.getValue(),valorFiltro.getText());
		loadDataSearch(newMap);
	}
	
	public void loadDataSearch(Map<Cliente, List<Arquivo>> mapa){
		
		mapa.forEach((k,v)->{
			v.forEach(e->{
				logServer.appendText(k.getNome()+" tem o arquivo "+e.getNome()+"\n");
				tableFiles.getColumns().get(0).setText("teste");
			});
		});
//		
//		List<Arquivo> listaTeste = new ArrayList<>();
//		
//		
//		Arquivo arquivo = new Arquivo();
//		arquivo.setId(0);
//		arquivo.setPath("meu path");
//		arquivo.setNome("teste");
//		arquivo.setMd5("meu md5");
//		arquivo.setDataHoraModificacao(new Date());
//		listaTeste.add(arquivo);
//		
//		for (Arquivo a: listaTeste) {
//            observableList.add(a);
//        }
//		
//		tableFiles.setItems(observableList);
		
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
