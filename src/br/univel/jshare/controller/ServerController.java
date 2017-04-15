package br.univel.jshare.controller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
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
import br.univel.jshare.observable.Observador;
import br.univel.jshare.util.Md5Util;
import br.univel.jshare.view.ViewMainController;

public class ServerController implements IServer{

	private List<Observador> observadores = new ArrayList<>();
	private List<Arquivo> listaArquivos = new ArrayList<>();
	private Map<Cliente, List<Arquivo>> mapaClientes = new HashMap<>();

	private IServer serviceClient;
	private Registry registryClient;
	private ViewMainController view;
	
	public ServerController() {}

	public void createServer(Cliente cliente){
		IServer service;
		ServerController server = new ServerController();
		try {
			service = (IServer) UnicastRemoteObject.exportObject(server, 0);
			Registry registry = LocateRegistry.createRegistry(cliente.getPorta());
			registry.rebind(IServer.NOME_SERVICO, service);		
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
	}
	
	public void conectarCliente(Cliente c, String ip, int porta, Observador observador){
		try {
			registryClient = LocateRegistry.getRegistry(ip, porta);
			serviceClient = (IServer) registryClient.lookup(IServer.NOME_SERVICO);
			serviceClient.registrarCliente(c);
			new Thread(new Runnable() {
				@Override
				public void run() {
					while(true){
						try {
							serviceClient.publicarListaArquivos(c, adicionarArquivos(listaArquivos));
						} catch (RemoteException e) {
							e.printStackTrace();
						}
						try {
							Thread.sleep(100000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void desconectarCliente(Cliente c){
		try {
			serviceClient.desconectar(c);
			
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
		File diretorio = new File("." + File.separatorChar + "share" + File.separatorChar);
		for(File file: diretorio.listFiles()){
			if(file.isFile()){
				Arquivo arq = new Arquivo();
				arq.setNome(file.getName());
				arq.setTamanho(file.length());
				arq.setPath(file.getPath());
				arq.setDataHoraModificacao(new Date(file.lastModified()));
				arq.setExtensao(pegarExtensao(arq.getNome()));
				arq.setMd5(Md5Util.getMD5Checksum(file.getAbsolutePath()));
				listab.add(arq);
			}
		}	
		return listab;
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

    public void adicionarObservador(final Observador observador) {
        this.observadores.add(observador);
    }

    public void notificarObservadores(final String text) {
    	observadores.forEach(observador -> observador.updateLog(text));
    }

    
	/**
	 * Metodos do servidor abaixo
	 */
	@Override
	public void registrarCliente(Cliente c) throws RemoteException {
		List<Arquivo> lista = new ArrayList<Arquivo>();
		if(!verificarCliente(c)){
			mapaClientes.put(c, lista);
			notificarObservadores("Cliente " + c.getNome() + " registrou-se no servidor!");
		}
	}
	
	@Override
	public void publicarListaArquivos(Cliente c, List<Arquivo> lista) throws RemoteException {
		if(mapaClientes.containsKey(c)){
			mapaClientes.entrySet().forEach(mapa->{
				if(mapa.getKey().equals(c)){
					mapa.setValue(lista);
				}
			});
		}
	}
	
	@Override
	public Map<Cliente, List<Arquivo>> procurarArquivo(String query, TipoFiltro tipoFiltro, String filtro)
			throws RemoteException {
		Map<Cliente, List<Arquivo>> newMap = new HashMap<>();
		mapaClientes.forEach((k,v)->{
			List<Arquivo> listFiles = new ArrayList<>();
			v.forEach(e->{
				switch (tipoFiltro) {
					case EXTENSAO:
						if(e.getExtensao().toLowerCase().contains(filtro.toLowerCase())){
							listFiles.add(e);
						}
					break;
					case NOME:
						if(e.getNome().toLowerCase().contains(query.toLowerCase())){
							listFiles.add(e);
						}
					break;
					case TAMANHO_MAX:
						int valor = Integer.parseInt(filtro);
						if(e.getTamanho() <= valor){
							listFiles.add(e);
						}
					break;
					case TAMANHO_MIN:
						int valor1 = Integer.parseInt(filtro);
						if(e.getTamanho() >= valor1){
							listFiles.add(e);
						}
					break;
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
		byte[] file;
		Path path = Paths.get(arq.getPath());
		try {
			file = Files.readAllBytes(path);
			System.out.println(cli.getNome() + " baixou o arquivo " + arq.getNome());
			return file;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void desconectar(Cliente c) throws RemoteException {
		if(mapaClientes.containsKey(c)){
			mapaClientes.remove(c);
		}
	}
	
}
