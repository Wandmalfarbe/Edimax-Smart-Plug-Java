package de.pascalwagler.edimaxsmartplug.smartplug;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.Scanner;

import de.pascalwagler.edimaxsmartplug.entities.PlugCredentials;

public class LocalHTTPConnection implements PlugConnection {

	private PlugCredentials credentials;
	private URL url;

	private String urlTemplate = "http://%s:10000/smartplug.cgi";
	
	public LocalHTTPConnection(PlugCredentials credentials, String ip) throws MalformedURLException {

		this.credentials = credentials;
		this.url = new URL(String.format(urlTemplate, ip));
	}
	
	@Override
	/**
	 * Does nothing because when communicating on local network 
	 * there's no connection to the cloud service.
	 */
	public void connect() {
		
	}
	
	@Override
	/**
	 * Always returns true because when communicating on local network 
	 * there's no connection to the cloud service.
	 */
	public boolean isConnected() {
		return false;
	}

	@Override
	public String sendCommand(String xml) throws Exception {

		if(!this.isConnected()) {
			this.connect();
		}
		
		InputStream input = null;
		Scanner scanner = null;
		BufferedWriter writer = null;
		BufferedOutputStream output = null;
		
		try {
			/*
			 * Request
			 */
			
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            
            byte[] auth = Base64.getEncoder().encode( (credentials.getUsername() + ":" + credentials.getPassword()).getBytes() );
			String basicAuthValue = new String(auth);
			
			urlConnection.setDoOutput(true);
			urlConnection.setRequestMethod("POST");
			urlConnection.setRequestProperty("Authorization", "Basic " + basicAuthValue);
			urlConnection.setRequestProperty("Connection", "close");
			
            output = new BufferedOutputStream(urlConnection.getOutputStream());
            writer = new BufferedWriter(new OutputStreamWriter(output, "UTF-8"));            		
            writer.write(xml);
            
            writer.flush();
            writer.close();
            output.close();

            urlConnection.connect();
            
			/*
			 * Response
			 */
			int statusCode = urlConnection.getResponseCode();
			
			if(statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
				// The credentials are wrong => the user is not authorized
				throw new Exception("Unauthorized: The supplied plug credentials are wrong.");
				
			} else if(statusCode != HttpURLConnection.HTTP_OK) {
				// Something else went wrong
				throw new Exception("Unknown error: The server responded with a status code that is not 200 OK.");
			}
			
			input = urlConnection.getInputStream();
			scanner = new Scanner(input);
			Scanner s = scanner.useDelimiter("\\A");
			String result = s.hasNext() ? s.next() : "";
			scanner.close();
			input.close();

			return result;
			
		} finally {
			if(input != null) input.close();
			if(scanner != null) scanner.close();
			if(writer != null) writer.close();
			if(output != null) output.close();
		}
	}
	
	@Override
	/**
	 * Unimplemented because when communicating on local network 
	 * there's no connection to the cloud service.
	 */
	public void disconnect() {
		
	}
}
