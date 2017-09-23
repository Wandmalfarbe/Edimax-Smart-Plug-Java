package de.pascalwagler.edimaxsmartplug.cli;

import java.net.MalformedURLException;
import java.time.LocalDateTime;

import de.pascalwagler.edimaxsmartplug.entities.PlugCredentials;
import de.pascalwagler.edimaxsmartplug.entities.SystemInformation;
import de.pascalwagler.edimaxsmartplug.smartplug.LocalConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.PlugConnection;
import de.pascalwagler.edimaxsmartplug.smartplug.SmartPlug;

public class MainTest {

	public static void main(String[] args) throws MalformedURLException {

		PlugCredentials credentials = new PlugCredentials("admin", "1234");
		PlugConnection connection = new LocalConnection(credentials, "192.168.178.34");
		SmartPlug smartPlug = new SmartPlug(connection);

		try {
			OutputFormatter oFormatter = new TerminalFormatter();
			
			/*float[] history = smartPlug.getHistory(SmartPlug.TimeUnit.DAY, LocalDateTime.now().minusHours(4000), LocalDateTime.now().plusHours(1));
			String historyStr = oFormatter.getHistory(history);
			System.out.println(historyStr);*/
			
			smartPlug.setName("Testname");
			SystemInformation info = smartPlug.getSystemInfo();
			System.out.println(info);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}