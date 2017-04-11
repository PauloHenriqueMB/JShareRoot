package br.univel.jshare.view;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.univel.jshare.MainApp;
import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;
import br.univel.jshare.controller.GenerateDialogController;
import br.univel.jshare.controller.ServerController;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

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
	private TextField valorFiltro;
	@FXML
	private StackPane painel;
	@FXML
	private Button realizarDownload;
	
	private TreeView<String> arvore;
	private TreeItem<String> raiz;
	private TreeItem<String> pai;
    private TreeItem<String> filho;
    
	private Date date;
	private boolean clientStatus = false;
	private boolean serverStatus = false;
	private Map<Cliente, List<Arquivo>> newMap = new HashMap<>();
	private ServerController serverController = new ServerController();
	private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	//Client data
	private Cliente cliente = new Cliente();
	
	public ViewMainController() {
	}
	
	@FXML
	public void initialize(){
		
		loadClientData();
		disableButtons(true);
		
		filtro.getItems().add(TipoFiltro.NOME);
		filtro.getItems().add(TipoFiltro.EXTENSAO);
		filtro.getItems().add(TipoFiltro.TAMANHO_MAX);
		filtro.getItems().add(TipoFiltro.TAMANHO_MIN);
		filtro.getSelectionModel().select(0);
		
	}
	
	public void loadClientData(){
		cliente.setId(0);
		cliente.setPorta(3000);
		cliente.setNome("SRVIEIRA");	
		try {
			cliente.setIp(InetAddress.getLocalHost().getHostAddress());
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
			serverController.createServer(cliente,this);
			handleServer.setText("Desligar");
			System.out.println(cliente.getIp());
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
				serverController.conectarCliente(cliente, ip, porta);
				clientStatus = true;
				disableButtons(false);
			}else{
				serverController.desconectarCliente(cliente);
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
		newMap = serverController.getArchives(fieldAssunto.getText(),filtro.getValue(),valorFiltro.getText());
		loadDataSearch(newMap);
	}
	
	public void loadDataSearch(Map<Cliente, List<Arquivo>> mapa){
		
		raiz = new TreeItem<>("Arquivos");
		mapa.forEach((k,v)->{
			pai = new TreeItem<>(k.getNome());
			v.forEach(e->{
				filho = new TreeItem<>(e.getNome());
				pai.getChildren().add(filho);
			});
			raiz.getChildren().add(pai);
		});
		arvore = new TreeView<>(raiz);
		arvore.getEditingItem();
		painel.getChildren().add(arvore);

	}
	
	@FXML
	public void realizarDownload(){
		String nome = String.valueOf(arvore.getSelectionModel().getSelectedItem().getParent().getValue());
		String arquivo = String.valueOf(arvore.getSelectionModel().getSelectedItem().getValue());
		
		newMap.forEach((k,v)->{
			if (k.getNome().equals(nome)) {
			
				v.forEach(e->{
					if(e.getNome().equals(arquivo)){
						try {
							
							Registry registry = LocateRegistry.getRegistry(k.getIp(), k.getPorta());
							IServer service = (IServer) registry.lookup(IServer.NOME_SERVICO);
							byte[] bytes = service.baixarArquivo(cliente, e);

							String fileName = nome.replace(" ", "").toLowerCase() +  ".copy." + arquivo;
								
							File f = new File(fileName);
							String filePath = "." + File.separatorChar + "share" + File.separatorChar + f;
							
							Path path = Files.write(Paths.get(filePath), bytes, StandardOpenOption.CREATE);
							
						} catch (RemoteException e1) {
							e1.printStackTrace();
						} catch (NotBoundException e1) {
							e1.printStackTrace();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						
					}
				});
				
			}
		});
	}
	
	@FXML
	public void limparBusca(){
		painel.getChildren().clear();
		fieldAssunto.clear();
		valorFiltro.clear();
	}
	
	public void setMainApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}
}
