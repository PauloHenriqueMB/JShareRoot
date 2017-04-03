package br.univel.jshare.controller;

import java.io.File;
import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.univel.jshare.comum.Arquivo;
import br.univel.jshare.comum.Cliente;
import br.univel.jshare.comum.IServer;
import br.univel.jshare.comum.TipoFiltro;

public class ServerController implements IServer{

	private Long id;
	private IServer server;
	private Registry registry;
	private List observer = new ArrayList<>();
	List<Arquivo> listaArquivos = new ArrayList<>();
	private Map<Cliente, List<Arquivo>> mapaClientes = new HashMap<>();
	
	public ServerController() {
	}

	public void createServer(Cliente cliente){
		try {
			server = (IServer) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.createRegistry(cliente.getPorta());
			registry.rebind(IServer.NOME_SERVICO, server);		
			registrarCliente(cliente);
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
	}
	
	public void closeServer(){
		try {
			UnicastRemoteObject.unexportObject(server, true);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}
	
	public Map<Cliente, List<Arquivo>> getMapaClientes() {
		return mapaClientes;
	}
	
	@Override
	public void registrarCliente(Cliente c) throws RemoteException {
		try {
			registry = LocateRegistry.getRegistry(c.getIp(), c.getPorta());
			server = (IServer) registry.lookup(IServer.NOME_SERVICO);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}		
		if(!verificarCliente(c)){
			publicarListaArquivos(c, listaArquivos);
			mapaClientes.put(c, listaArquivos);			
		}
		
	}

	public boolean verificarCliente(Cliente c){
		mapaClientes.forEach((k,v)->{
			if(k.getNome().equals(c.getNome())){
				System.out.println("ja tem nome registrado");
				return;
			}
		});
		return false;
	}
	
	@Override
	public void publicarListaArquivos(Cliente c, List<Arquivo> lista) throws RemoteException {
		File diretorio = new File("." + File.separatorChar + "share" + File.separatorChar + "uploads");
		for(File file: diretorio.listFiles()){
			if(file.isFile()){
				Arquivo arq = new Arquivo();
				arq.setNome(file.getName());
				arq.setTamanho(file.length());
				lista.add(arq);
			}
		}
	}

	@Override
	public Map<Cliente, List<Arquivo>> procurarArquivo(String query, TipoFiltro tipoFiltro, String filtro)
			throws RemoteException {
		return null;
	}

	@Override
	public byte[] baixarArquivo(Cliente cli, Arquivo arq) throws RemoteException {
		return null;
	}

	@Override
	public void desconectar(Cliente c) throws RemoteException {
	}

}
