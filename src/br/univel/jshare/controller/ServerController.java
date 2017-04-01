package br.univel.jshare.controller;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NoSuchObjectException;
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

	private IServer server;
	private Cliente cliente;
	private Registry registry;
	private InetAddress address;
	private Map<Cliente, List<Arquivo>> mapaClientes = new HashMap<>();

	public ServerController() {
	}

	public void createServer(){
		cliente = new Cliente();
		cliente.setNome("SRVIEIRA");
		try {
			cliente.setPorta(3000);
			cliente.setIp(address.getLocalHost().toString());
			server = (IServer) UnicastRemoteObject.exportObject(this, 0);
			registry = LocateRegistry.createRegistry(3000);
			registry.rebind(IServer.NOME_SERVICO, server);		
			registrarCliente(cliente);
		} catch (RemoteException | UnknownHostException e) {
			e.printStackTrace();
		} 
	}
	
	public void closeServer(){
		try {
			UnicastRemoteObject.unexportObject(this, true);
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}
	
	public Map<Cliente, List<Arquivo>> getMapaClientes() {
		return mapaClientes;
	}
	
	@Override
	public void registrarCliente(Cliente c) throws RemoteException {
		List<Arquivo> list = new ArrayList<>();
		mapaClientes.put(c, list);
	}

	@Override
	public void publicarListaArquivos(Cliente c, List<Arquivo> lista) throws RemoteException {
		
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
