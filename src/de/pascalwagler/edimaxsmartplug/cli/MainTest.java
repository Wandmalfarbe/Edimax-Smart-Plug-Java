package de.pascalwagler.edimaxsmartplug.cli;

import de.pascalwagler.edimaxsmartplug.entities.PlugCredentials;
import de.pascalwagler.edimaxsmartplug.smartplug.LocalConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.PlugConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug;

public class MainTest {

	public static void main(String[] args) {
		
		PlugCredentials credentials = new PlugCredentials("admin", "1234");
		PlugConnection connection = new LocalConnection(credentials, "192.168.178.34");
		SmartPlug smartPlug = new SmartPlug(connection);
		
		try {
			String sysTime = smartPlug.getSystemTime();
			//String powInfoStr = oFormatter.getPowerInformation(sysTime);
			System.out.println(sysTime);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}