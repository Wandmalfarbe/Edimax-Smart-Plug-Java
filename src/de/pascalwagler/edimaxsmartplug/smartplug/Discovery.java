package de.pascalwagler.edimaxsmartplug.smartplug;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class Discovery {

	private static final int PACKET_STATUS_REQUEST = 0;
	private static final int PACKET_STATUS_RESPONSE = 1;
	private static final int PACKET_STATUS_FAULT = 2;

	private static final int PACKET_OFFSET_MAC_ADDR = 0;
	private static final int PACKET_LENGTH_MAC_ADDR = 6;

	private static final int PACKET_OFFSET_STATUS = 18;

	private static final int PACKET_OFFSET_UNKNOWN_STATUS = 19; // maybe some kind of status

	private static final int PACKET_OFFSET_MODEL_NAME = 22;
	private static final int PACKET_LENGTH_MODEL_NAME = 14;

	private static final int PACKET_OFFSET_FIRMWARE_VERSION = 36;
	private static final int PACKET_LENGTH_FIRMWARE_VERSION = 8;

	private static final int PACKET_OFFSET_NAME = 44;
	private static final int PACKET_LENGTH_NAME = 128;

	private static final int PACKET_OFFSET_WEB_PORT = 172;
	private static final int PACKET_LENGTH_WEB_PORT = 2;

	private static final int PACKET_OFFSET_IP_ADDR = 174;
	private static final int PACKET_LENGTH_IP_ADDR = 4;

	private static final int PACKET_OFFSET_SUBNET = 178;
	private static final int PACKET_LENGTH_SUBNET = 4;

	private static final int PACKET_OFFSET_GATEWAY = 182;
	private static final int PACKET_LENGTH_GATEWAY = 4;
	
	private static final int PACKET_LENGTH_HEADER = 22;
	private static final int PACKET_LENGTH_SIGNATURE = 12;
	
	private static final int AGENT_PORT = 20560;
	private static final int RECEIVER_SIZE = 186;
	
	private static final String SIGNATURE = "EDIMAX";

	public static class DeviceInformation {
		public int port;
		public String firmwareVersion;
		public String gatewayAddress;
		public String IPAddress;
		public String macAddress;
		public String modelName;
		public String name;
		public String subnetMask;
		
		public byte[] bsMacAddr;

		public DeviceInformation() {
			
			this.bsMacAddr = new byte[PACKET_LENGTH_MAC_ADDR];
			
			this.macAddress = "";
			this.IPAddress = "";
			this.subnetMask = "";
			this.gatewayAddress = "";
			this.port = 0;
			this.name = "";
			this.modelName = "";
			this.firmwareVersion = "";
		}

		@Override
		public String toString() {
			return "\"DeviceInformation\": {\n\t\"port\": \"" + port + "\", \n\t\"firmwareVersion\": \""
					+ firmwareVersion + "\", \n\t\"gatewayAddress\": \"" + gatewayAddress + "\", \n\t\"IPAddress\": \""
					+ IPAddress + "\", \n\t\"macAddress\": \"" + macAddress + "\", \n\t\"modelName\": \"" + modelName
					+ "\", \n\t\"name\": \"" + name + "\", \n\t\"subnetMask\": \"" + subnetMask
					+ "\", \n\t\"bsMacAddr\": \"" + Arrays.toString(bsMacAddr) + "\"\n}";
		}
	}

	/**
	 * Only for quick testing.
	 */
	public static void main(String[] args) {
		Discovery.runSearch();
	}
	
	private Discovery() {
		
	}

	public static byte[] getSearchHeader() {

		byte[] searchHeaderBytes = new byte[PACKET_LENGTH_HEADER];

		/*
		 * Fill first 6 Bytes with -1 (or FF in hex)
		 */
		for (int n = 0; n < PACKET_LENGTH_MAC_ADDR; n++) {
			searchHeaderBytes[n] = (byte) -1;
		}

		/*
		 * Fill from byte 6  to byte 12(excluding) (6 bytes) the signature (EDIMAX)
		 * Fill from byte 12 to byte 18(excluding) (6 bytes) with 0
		 */
		for (int index = 0; index < PACKET_LENGTH_SIGNATURE; index++) {
			if (index < SIGNATURE.length()) {
				searchHeaderBytes[index + PACKET_LENGTH_MAC_ADDR] = (byte) SIGNATURE.charAt(index);
			} else {
				searchHeaderBytes[index + PACKET_LENGTH_MAC_ADDR] = (byte) 0;
			}
		}

		/*
		 * Fill from byte 18 to 21(including)
		 */
		searchHeaderBytes[18] = (byte) 0;
		searchHeaderBytes[19] = (byte) -95;
		searchHeaderBytes[20] = (byte) -1;
		searchHeaderBytes[21] = (byte) 94;

		return searchHeaderBytes;
	}

	public static ArrayList<String> runSearch() {

		ArrayList<String> mPlugList = new ArrayList<String>();
		byte[] searchPacket = getSearchHeader();
		byte[] replyPacket = new byte[256];
		DatagramSocket datagramSocket = null;

		try {
			// Create UDP Socket
			datagramSocket = new DatagramSocket();
			datagramSocket.setSoTimeout(1000);
			datagramSocket.setBroadcast(true);

			// Create UDP Packet
			InetAddress hostAddress = InetAddress.getByName("255.255.255.255");
			DatagramPacket p = new DatagramPacket(searchPacket, searchPacket.length, hostAddress, AGENT_PORT);
			writeByteArrToFile(p.getData(), "sent_datagram_packet.bin");


			// Send UDP Packet
			datagramSocket.send(p);

			// Receive reply
			try {
				Arrays.fill(replyPacket, (byte) 0);

				DatagramPacket responsePacket = new DatagramPacket(replyPacket, replyPacket.length);
				datagramSocket.receive(responsePacket);
				writeByteArrToFile(responsePacket.getData(), "received_datagram_packet.bin");

				DeviceInformation info = Discovery.parseResponsePacket(responsePacket);
				System.out.println(info);

			} catch (SocketTimeoutException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}

		} catch (SocketException e3) {
			e3.printStackTrace();
		} catch (UnknownHostException e4) {
			e4.printStackTrace();
		} catch (IOException e5) {
			e5.printStackTrace();
		} finally {
			if(datagramSocket != null) datagramSocket.close();
		}

		return mPlugList;
	}

	/**
	 * For debugging. Can write the UDP packet to a file.
	 * 
	 * @param byteArr
	 * @param filename
	 */
	private static void writeByteArrToFile(byte[] byteArr, String filename) {

		FileOutputStream stream = null;
		try {
			stream = new FileOutputStream(filename);
			stream.write(byteArr);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final String IPV4_ADDR_FORMAT_STR = "%d.%d.%d.%d";
	private static final String MAC_ADDR_FORMAT_STR = "%02X%02X%02X%02X%02X%02X";

	private static DeviceInformation parseResponsePacket(DatagramPacket datagramPacket) throws Exception {

		byte[] tempByteArray = new byte[196];
		byte[] packetData = datagramPacket.getData();
		int packetLength = datagramPacket.getLength();

		// Packet marked as fault
		if (packetData[PACKET_OFFSET_STATUS] == PACKET_STATUS_FAULT) {
			throw new Exception("Error: The packet was marked as fault.");
		}

		// This was not a response packet
		if (packetData[PACKET_OFFSET_STATUS] != PACKET_STATUS_RESPONSE) {
			throw new Exception("Error: The packed was not a response packet.");
		}

		// ?
		if (	packetData[PACKET_OFFSET_UNKNOWN_STATUS] != -95) {
			throw new Exception("Error: Some unknown status had the wrong value.");
		}

		// Packet too small
		if (	RECEIVER_SIZE > packetLength) {
			throw new Exception("Error: The packet length is smaller than "+RECEIVER_SIZE);
		}

		DeviceInformation deviceInfo = new DeviceInformation();

		// MAC Address
		System.arraycopy(packetData, PACKET_OFFSET_MAC_ADDR, deviceInfo.bsMacAddr, 0, PACKET_LENGTH_MAC_ADDR); // Mac address has length of 6 bytes
		Object[] macAddrParts = new Object[PACKET_LENGTH_MAC_ADDR];
		macAddrParts[0] = Byte.valueOf(packetData[0]);
		macAddrParts[1] = Byte.valueOf(packetData[1]);
		macAddrParts[2] = Byte.valueOf(packetData[2]);
		macAddrParts[3] = Byte.valueOf(packetData[3]);
		macAddrParts[4] = Byte.valueOf(packetData[4]);
		macAddrParts[5] = Byte.valueOf(packetData[5]);
		deviceInfo.macAddress = String.format(MAC_ADDR_FORMAT_STR, macAddrParts);

		// Model name
		Arrays.fill(tempByteArray, (byte) 0);
		System.arraycopy(packetData, PACKET_OFFSET_MODEL_NAME, tempByteArray, 0, PACKET_LENGTH_MODEL_NAME);
		deviceInfo.modelName = new String(tempByteArray).trim();

		// Firmware Version
		Arrays.fill(tempByteArray, (byte) 0);
		System.arraycopy(packetData, PACKET_OFFSET_FIRMWARE_VERSION, tempByteArray, 0, PACKET_LENGTH_FIRMWARE_VERSION);
		deviceInfo.firmwareVersion = new String(tempByteArray).trim();

		// Device Name
		Arrays.fill(tempByteArray, (byte) 0);
		System.arraycopy(packetData, PACKET_OFFSET_NAME, tempByteArray, 0, PACKET_LENGTH_NAME);
		deviceInfo.name = new String(tempByteArray).trim();

		// Port
		deviceInfo.port = byteToShort(packetData[PACKET_OFFSET_WEB_PORT+1], packetData[PACKET_OFFSET_WEB_PORT]);

		// IP Address
		Arrays.fill(tempByteArray, (byte) 0);
		System.arraycopy(packetData, PACKET_OFFSET_IP_ADDR, tempByteArray, 0, PACKET_LENGTH_IP_ADDR);
		deviceInfo.IPAddress = String.format(IPV4_ADDR_FORMAT_STR, 
				Integer.valueOf(tempByteArray[0] & 255), 
				Integer.valueOf(tempByteArray[1] & 255), 
				Integer.valueOf(tempByteArray[2] & 255), 
				Integer.valueOf(tempByteArray[3] & 255));

		// Subnet Mask
		Arrays.fill(tempByteArray, (byte) 0);
		System.arraycopy(packetData, PACKET_OFFSET_SUBNET, tempByteArray, 0, PACKET_LENGTH_SUBNET);
		deviceInfo.subnetMask = String.format(IPV4_ADDR_FORMAT_STR, 
				Integer.valueOf(tempByteArray[0] & 255), 
				Integer.valueOf(tempByteArray[1] & 255), 
				Integer.valueOf(tempByteArray[2] & 255), 
				Integer.valueOf(tempByteArray[3] & 255));

		// Gateway Address
		Arrays.fill(tempByteArray, (byte) 0);
		System.arraycopy(packetData, PACKET_OFFSET_GATEWAY, tempByteArray, 0, PACKET_LENGTH_GATEWAY);
		deviceInfo.gatewayAddress = String.format(IPV4_ADDR_FORMAT_STR, 
				Integer.valueOf(tempByteArray[0] & 255), 
				Integer.valueOf(tempByteArray[1] & 255), 
				Integer.valueOf(tempByteArray[2] & 255), 
				Integer.valueOf(tempByteArray[3] & 255));

		return deviceInfo;
	}

	public static int byteToShort(byte b1, byte b2) {
		return ((b1 << 8) & 65280) + (b2 & 255);
	}
}
