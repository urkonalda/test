test
package net21;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class DateUdpClient {
	private static final int PORT = 8080;
	
	public static void main(String [] args) {
		byte message[] = new byte[256];
				
		try (
				DatagramSocket client = new DatagramSocket()
		){
			DatagramPacket sendPacket = new DatagramPacket(message, message.length, InetAddress.getByName("localhost"), PORT);
			client.send(sendPacket);
			
			DatagramPacket receivePacket = new DatagramPacket(message, message.length);
			client.receive(receivePacket);
			
			String date  = new String(receivePacket.getData(),0,receivePacket.getLength());
			System.out.println("La hora es " + date);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
