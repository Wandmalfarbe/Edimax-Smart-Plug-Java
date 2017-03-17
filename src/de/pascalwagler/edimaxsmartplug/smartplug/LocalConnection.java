package de.pascalwagler.edimaxsmartplug.smartplug;

import java.io.IOException;

import javax.xml.ws.http.HTTPException;

import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MIME;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import de.pascalwagler.edimaxsmartplug.entities.PlugCredentials;

import java.util.Base64;

public class LocalConnection implements PlugConnection {

	private PlugCredentials credentials;
	private String url;

	private String urlTemplate = "http://%s:10000/smartplug.cgi";
	private CloseableHttpClient httpClient;
	
	public LocalConnection(PlugCredentials credentials, String ip) {

		this.credentials = credentials;
		this.url = String.format(urlTemplate, ip);
	}
	
	@Override
	public void connect() {
		
		RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5 * 1000).setConnectionRequestTimeout(5 * 1000).build();
		httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
	}
	
	@Override
	public boolean isConnected() {
		return httpClient != null;
	}

	@Override
	public String sendCommand(String xml) throws IOException, ClientProtocolException, HTTPException{

		if(!this.isConnected()) {
			this.connect();
		}
		
		CloseableHttpResponse response = null;
		
		try {
			/*
			 * Request
			 */
			
			byte[] auth = Base64.getEncoder().encode( (credentials.getUsername() + ":" + credentials.getPassword()).getBytes() );
			String httpAuth = new String(auth);

			HttpPost httpPostRequest = new HttpPost(this.url);
			httpPostRequest.setHeader("Authorization", "Basic " + httpAuth);
			httpPostRequest.setHeader("Connection", "close");

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("xml", xml, ContentType.create("text/xml", MIME.UTF8_CHARSET));

			HttpEntity multipartEntity = builder.build();
			httpPostRequest.setEntity(multipartEntity);

			response = httpClient.execute(httpPostRequest);
			
			/*
			 * Response
			 */
			StatusLine statusLine = response.getStatusLine();
			
			if(statusLine.getStatusCode() == 401) {
				
				// The credentials are wrong => the user is not authorized
				throw new HTTPException(statusLine.getStatusCode());
				
			} else if(statusLine.getStatusCode() != 200) {
				
				// We got any other HTTP status code except 200
				throw new HTTPException(statusLine.getStatusCode());
			}

			HttpEntity responseEntity = response.getEntity();
			String responseText = EntityUtils.toString(responseEntity, "UTF-8");

			return responseText;
			
		} finally {
			if (response != null) response.close();
		}
	}
	
	@Override
	public void disconnect() {
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			httpClient = null;
		}
	}
}
