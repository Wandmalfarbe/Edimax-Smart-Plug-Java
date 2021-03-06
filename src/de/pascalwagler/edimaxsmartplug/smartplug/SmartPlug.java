package de.pascalwagler.edimaxsmartplug.smartplug;
import java.io.StringReader;
import java.time.LocalDateTime;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import de.pascalwagler.edimaxsmartplug.entities.PowerInformation;
import de.pascalwagler.edimaxsmartplug.entities.ScheduleDay;
import de.pascalwagler.edimaxsmartplug.entities.SystemInformation;


public class SmartPlug {

	private PlugConnection connection;

	public static enum TimeUnit {
		HOUR, DAY, MONTH 
	}

	public static enum State {
		ON, OFF
	}

	public SmartPlug(PlugConnection connection) {
		this.connection = connection;
	}

	/**
	 * Only for debugging.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private void getAll() throws Exception {

		String xml = RequestTemplates.getAll();
		String response = this.connection.sendCommand(xml);
		System.out.println(response);		
	}

	// TODO Parse time response.
	public String getSystemTime() throws Exception {

		String xml = RequestTemplates.getGetSystemTime();
		String response = this.connection.sendCommand(xml);
		return response;
	}

	public ScheduleDay[] getSchedule() throws Exception {

		String xml = RequestTemplates.getGetSchedule();
		String response = this.connection.sendCommand(xml);

		return Schedule.createFromDocument(EdimaxUtil.getDocumentFromString(response));
	}

	public State getState() throws Exception {

		String xml = RequestTemplates.getGetStatus();
		String response = this.connection.sendCommand(xml);

		InputSource source = new InputSource(new StringReader(response));
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String statusString = xpath.evaluate("/SMARTPLUG/CMD/Device.System.Power.State", source);

		if(statusString.equals("ON")) {
			return SmartPlug.State.ON;
		}	
		return SmartPlug.State.OFF;
	}

	public PowerInformation getPowerInformation() throws Exception {

		String xml = RequestTemplates.getGetPowerInfo();
		String response = this.connection.sendCommand(xml);

		Document document = EdimaxUtil.getDocumentFromString(response);
		PowerInformation powerInfo = PowerInformation.createFromDocument(document);

		return powerInfo;
	}

	// TODO Parse power usage response.
	public String getPowerUsage() throws Exception {

		String xml = RequestTemplates.getPowerUsage();
		String response = this.connection.sendCommand(xml);

		return response;
	}

	public SystemInformation getSystemInformation() throws Exception {

		String xml = RequestTemplates.getGetSystemInfo();
		String response = this.connection.sendCommand(xml);

		Document document = EdimaxUtil.getDocumentFromString(response);
		SystemInformation systemInfo = SystemInformation.createFromDocument(document);

		return systemInfo;
	}

	// TODO Prevent adding of values when querying for a day history.
	public float[] getHistory(SmartPlug.TimeUnit timeUnit, LocalDateTime from, LocalDateTime to) throws Exception {
		
		String xml = RequestTemplates.getGetHistory(timeUnit, from, to);
		String response = this.connection.sendCommand(xml);

		InputSource source = new InputSource(new StringReader(response));
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();

		String historyString = xpath.evaluate("/SMARTPLUG/CMD/POWER_HISTORY/Device.System.Power.History.Energy", source);

		/**
		 * Split the history string into parts
		 * which are separated by '-' and decode each part.
		 */
		String[] arr = historyString.split("-");
		float[] doubleArr = new float[arr.length];

		for(int x = 0; x < arr.length; x++) {
			doubleArr[x] = decodeHistoryPart(arr[x]);
			/*if(timeUnit == SmartPlug.TimeUnit.DAY && ) {
				
			}
			if (this.mMeter_Type == meter_form_type.day && count % 24 != 0) {
                _NewValue -= _pre_value;
            }*/
		}

		return doubleArr;
	}

	private float decodeHistoryPart(String part) throws Exception {

		float result = 0;

		if (part.equals("=")) {
			return 0;
		}

		for (int i = 0; i < part.length(); i++) {
			int charCode = Character.codePointAt(part, part.length() - i - 1);
			int thisNumber = EdimaxUtil.numberFromCharCode(charCode);
			result += thisNumber * (Math.pow(64, i));
		}

		return result / 1000;
	}

	public void setName(String name) throws Exception {

		String xml = RequestTemplates.getSetName(name);
		this.connection.sendCommand(xml);
	}

	public void toggle() throws Exception {
		State status = this.getState();
		if(status == SmartPlug.State.ON) {
			this.switchOff();
		} else {
			this.switchOn();
		}
	}

	public void switchOn() throws Exception {

		String xml = RequestTemplates.getSwitchOn();
		this.connection.sendCommand(xml);
	}

	public void switchOff() throws Exception {

		String xml = RequestTemplates.getSwitchOff();
		this.connection.sendCommand(xml);
	}
}