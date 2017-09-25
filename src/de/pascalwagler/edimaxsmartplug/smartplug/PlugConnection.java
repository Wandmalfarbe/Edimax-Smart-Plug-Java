package de.pascalwagler.edimaxsmartplug.smartplug;

import java.io.IOException;

public interface PlugConnection {
	
	public void connect() throws IOException;
	
	public boolean isConnected();
	
	public String sendCommand(String xmlCommand) throws Exception;
	
	public void disconnect() throws IOException;
}