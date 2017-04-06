package br.univel.jshare.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;
import br.univel.jshare.view.ViewMainController;

public class ServerController implements IServer{

	private Long id;	
	private List observer = new ArrayList<>();
	private List<Arquivo> listaServidor = new ArrayList<>();
	private List<Arquivo> listaArquivos = new ArrayList<>();
	private Map<Cliente, List<Arquivo>> mapaClientes = new HashMap<>();

	private IServer serviceClient;
	private Registry registryClient;

	private ViewMainController viewMain;
	
	public ServerController() {
	}

	public void createServer(Cliente cliente, ViewMainController view){
		
		IServer service;
		ServerController server = new ServerController();
		viewMain = view;
		
		try {
			
			service = (IServer) UnicastRemoteObject.exportObject(server, 0);
			Registry registry = LocateRegistry.createRegistry(cliente.getPorta());
			registry.rebind(IServer.NOME_SERVICO, service);		

		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		
	}
	
	public void conectarCliente(Cliente c, String ip, int porta){
		
		System.out.println(c);
		
		try {
	
			registryClient = LocateRegistry.getRegistry(ip, porta);
			serviceClient = (IServer) registryClient.lookup(IServer.NOME_SERVICO);
			serviceClient.registrarCliente(c);
			serviceClient.publicarListaArquivos(c, adicionarArquivos(listaArquivos));
//			viewMain.setUserConnected(c.getNome());
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
		
	}
	
	public void desconectarCliente(Cliente c){
		try {
			serviceClient.desconectar(c);
			viewMain.setUserDisconnected(c.getNome());
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
	}

	public boolean verificarCliente(Cliente c){
		if(mapaClientes.containsKey(c)){
			return true;
		}
		return false;
	}
	
	public Map<Cliente, List<Arquivo>> getMapaClientes() {
		return mapaClientes;
	}
	
	public String pegarExtensao(String arquivo){
		String extensao = "";
		int i = arquivo.lastIndexOf('.');
		int p = Math.max(arquivo.lastIndexOf('/'), arquivo.lastIndexOf('\\'));
		if(i > p){
			extensao = arquivo.substring(i+1);
		}
		return extensao;
	}
	

	public List<Arquivo> adicionarArquivos(List<Arquivo> lista){
		List<Arquivo> listab = new ArrayList<>();
//		DateFormat formatData = new SimpleDateFormat("dd/MM/yyyy");   
		File diretorio = new File("." + File.separatorChar + "share");
		for(File file: diretorio.listFiles()){
			if(file.isFile()){
				Arquivo arq = new Arquivo();
				arq.setNome(file.getName());
				arq.setTamanho(file.length());
				arq.setPath(file.getPath());
				arq.setDataHoraModificacao(new Date(file.lastModified()));
				arq.setExtensao(pegarExtensao(arq.getNome()));
				try {
					arq.setMd5(pegarHashArquivo(file));
				} catch (NoSuchAlgorithmException | FileNotFoundException e) {
					e.printStackTrace();
				}
				listab.add(arq);
			}
		}	
		return listab;
	}

	public String pegarHashArquivo(File arquivo) throws NoSuchAlgorithmException, FileNotFoundException{
		MessageDigest digest = MessageDigest.getInstance("MD5");
		InputStream input = new FileInputStream(arquivo);
		byte[] buffer = new byte[8192];
		int read = 0;
		String output = null;
		try {
			while( (read = input.read(buffer)) > 0) {
				digest.update(buffer, 0, read);
			}		
			byte[] md5sum = digest.digest();
			BigInteger bigInt = new BigInteger(1, md5sum);
			output = bigInt.toString(16);
		}
		catch(IOException e) {
			throw new RuntimeException("Não foi possivel processar o arquivo.", e);
		}
		finally {
			try {
				input.close();
			}
			catch(IOException e) {
				throw new RuntimeException("Não foi possivel fechar o arquivo", e);
			}
		}	
		return output;
	}
	
	public Map<Cliente, List<Arquivo>> getArchives(String arquivo, TipoFiltro tipoFiltro, String valorfiltro){
		Map<Cliente, List<Arquivo>> newMap = new HashMap<>();
		try {
			newMap = serviceClient.procurarArquivo(arquivo, tipoFiltro, valorfiltro);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return newMap;
	}
	
	@Override
	public void registrarCliente(Cliente c) throws RemoteException {

		List<Arquivo> list = new ArrayList<>();
		
		if(!verificarCliente(c)){
			mapaClientes.put(c, list);
		}
		
	}
	
	@Override
	public void publicarListaArquivos(Cliente c, List<Arquivo> lista) throws RemoteException {
		if(mapaClientes.containsKey(c)){
			mapaClientes.entrySet().forEach(mapa->{
				if(mapa.getKey().equals(c)){
					mapa.setValue(lista);
					System.out.println(c.getNome()+" conectou");
					System.out.println(lista);
				}else{
				}
			});
		}
	}
	
	@Override
	public Map<Cliente, List<Arquivo>> procurarArquivo(String query, TipoFiltro tipoFiltro, String filtro)
			throws RemoteException {
		
		
		Map<Cliente, List<Arquivo>> newMap = new HashMap<>();
		
		Cliente client = new Cliente();
		
		mapaClientes.forEach((k,v)->{
			List<Arquivo> listFiles = new ArrayList<>();
			v.forEach(e->{
				if(tipoFiltro == null){
					listFiles.add(e);
				}else{
					switch (tipoFiltro) {
						case EXTENSAO:
							break;
						case NOME:
							break;
						case TAMANHO_MAX:
							int valor = Integer.parseInt(filtro);
							if(e.getTamanho() <= valor){
							
							}else{
							
							}
							break;
						case TAMANHO_MIN:
							break;
						default:
							if(e.getNome().toLowerCase().contains(query.toLowerCase())){
								listFiles.add(e);
							}
						break;
					}
				}
			});	
			newMap.put(k, listFiles);
		});
		
		return newMap;
	}
	
	public boolean verifyIfContainsName(String query, String fileName){
		if(fileName.toLowerCase().contains(query.toLowerCase())){
			return true;
		}
		return false;
	}

	@Override
	public byte[] baixarArquivo(Cliente cli, Arquivo arq) throws RemoteException {
		return null;
	}
	
	@Override
	public void desconectar(Cliente c) throws RemoteException {
		
	}

}
