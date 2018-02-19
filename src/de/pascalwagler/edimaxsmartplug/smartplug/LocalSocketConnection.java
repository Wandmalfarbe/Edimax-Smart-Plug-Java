package de.pascalwagler.edimaxsmartplug.smartplug;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import de.pascalwagler.edimaxsmartplug.entities.PlugCredentials;

public class LocalSocketConnection implements PlugConnection {

	private PlugCredentials credentials;
	private String ipAddress;

	private Socket socket;
	
	private static final int SOCKET_PORT = 5678;
	private static final int SOCKET_TIMEOUT = 10000; // 10 seconds

	public LocalSocketConnection(PlugCredentials credentials, String ipAddress) {

		this.credentials = credentials;
		this.ipAddress = ipAddress;
	}

	@Override
	public void connect() throws IOException {
		this.socket = new Socket();
		this.socket.connect(new InetSocketAddress(ipAddress, SOCKET_PORT), SOCKET_TIMEOUT);
	}

	@Override
	public boolean isConnected() {
		if (this.socket != null) {
			return this.socket.isConnected();
		}
		return false;
	}

	@Override
	public String sendCommand(String xmlCommand) throws Exception {

		throw new Exception("The local socket connection method is currently unsupported.");
		
		/*if(!this.isConnected()) {
			this.connect();
		}

		String _xmlData = xmlCommand; // sadly this has to be JSON
		
		try {
			OutputStream out = this.mSocket.getOutputStream();
			out.write(xmlCommand.getBytes());
			out.flush();
			out.close();
		} finally {
			// Close everything
		}

		return "";*/
	}

	@Override
	public void disconnect() throws IOException {
		if (this.socket != null && this.socket.isConnected()) {
			this.socket.close();
		}
		this.socket = null;
	}
}