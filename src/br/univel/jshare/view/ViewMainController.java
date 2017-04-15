package br.univel.jshare.view;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
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
import br.univel.jshare.observable.Observador;
import br.univel.jshare.util.Md5Util;
import br.univel.jshare.util.ValidadorIp;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
	
public class ViewMainController implements Observador{
	
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
	    
		private Date data;
		private boolean statusCliente = false;
		private boolean statusServer = false;
		private Map<Cliente, List<Arquivo>> novoMapa = new HashMap<>();
		private ServerController serverController = new ServerController();
		private DateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
		//Client data
		private Cliente cliente = new Cliente();
		
		public ViewMainController() {
			serverController.adicionarObservador(this);
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
		
		@FXML
		public void disableButtons(boolean status){
			btnLimpar.setDisable(status);
			btnBuscar.setDisable(status);
			fieldAssunto.setDisable(status);
			filtro.setDisable(status);
			realizarDownload.setDisable(status);
		}
		
		@FXML
		public void handleServer(){
			data = new Date();
			if(!statusServer){
				statusServer = true;
				serverController.createServer(cliente);
				handleServer.setText("Desligar");
				logServer.appendText("Servidor iniciado "+formatoData.format(data)+"\n");
				fieldIp.setText(cliente.getIp());
				fieldPort.setText(Integer.toString(cliente.getPorta()));
			}else{
				System.exit(0);
			}
		}
		
		@FXML
		public void handleConnection(){

			if(new ValidadorIp().validar(fieldIp.getText()) == true && fieldPort.getLength() == 4){
				String ip = fieldIp.getText();
				int porta = Integer.parseInt(fieldPort.getText());
				if(!statusCliente){
					handleCon.setText("Desconectar");
					serverController.conectarCliente(cliente, ip, porta, this);
					statusCliente = true;
					disableButtons(false);
				}else{
					serverController.desconectarCliente(cliente);
					handleCon.setText("Conectar");
					valorFiltro.setDisable(true);
					statusCliente = false;
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
			realizarDownload.setDisable(false);
			novoMapa = serverController.getArchives(fieldAssunto.getText(),filtro.getValue(),valorFiltro.getText());
			loadDataSearch(novoMapa);
		}
		
		@FXML
		public void realizarDownload(){
			
			if(arvore.getSelectionModel().getSelectedItem() != null){
				String nome = String.valueOf(arvore.getSelectionModel().getSelectedItem().getParent().getValue());
				String arquivo = String.valueOf(arvore.getSelectionModel().getSelectedItem().getValue());
				
				novoMapa.forEach((k,v)->{
					if (k.getNome().equals(nome)) {
					
						v.forEach(e->{
							if(e.getNome().equals(arquivo)){
								try {
									
									Registry registry = LocateRegistry.getRegistry(k.getIp(), k.getPorta());
									IServer service = (IServer) registry.lookup(IServer.NOME_SERVICO);
									byte[] bytes = service.baixarArquivo(cliente, e);
		
									File f = new File("." + File.separatorChar + "share/download " + e.getNome() + "." + e.getExtensao());
									Files.write(Paths.get(f.getPath()), bytes, StandardOpenOption.CREATE);
									
									File download = new File("." + File.separatorChar + "share/download " + e.getNome() + "." + e.getExtensao());
									
									if(e.getMd5().equals(Md5Util.getMD5Checksum(download.getAbsolutePath()))){
										new GenerateDialogController()
											.generateDialog(AlertType.INFORMATION, "Download Concluido", null, "O seu download do arquivo "+e.getNome()+" foi realizado com sucesso!");
									}else{
										new GenerateDialogController()
										.generateDialog(AlertType.INFORMATION, "Falha no download", null, "O download não foi concluido pois pode estar corrompido!");								
									}
									
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
			}else{
				new GenerateDialogController()
				.generateDialog(AlertType.INFORMATION, "Falha no download", null, "Nenhum arquivo foi selecionado para download!");												
			}
		}
		
		@FXML
		public void limparBusca(){
			painel.getChildren().clear();
			fieldAssunto.clear();
			valorFiltro.clear();
		}
		
		@FXML
		public void verificarTipo(){
			if(filtro.getValue() != TipoFiltro.NOME){
				valorFiltro.setDisable(false);
			}else{
				valorFiltro.setDisable(true);
			}
		}
		
		public void loadDataSearch(Map<Cliente, List<Arquivo>> mapa){
			raiz = new TreeItem<>("Arquivos");
			mapa.forEach((k,v)->{
				if(!k.getIp().equals(cliente.getIp())){
					pai = new TreeItem<>(k.getNome());
					v.forEach(e->{
						filho = new TreeItem<>(e.getNome());
						pai.getChildren().add(filho);
					});
					raiz.getChildren().add(pai);
				}
			});
			arvore = new TreeView<>(raiz);
			arvore.getEditingItem();
			painel.getChildren().add(arvore);
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
		
		public void setMainApp(MainApp mainApp) {
			this.mainApp = mainApp;
		}
	
		@Override
		public void updateLog(String logText) {
			logServer.appendText(logText+"\n");
			System.out.println(logText);
		}

}
