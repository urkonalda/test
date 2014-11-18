package net21;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;

public class DateUdpServer {
	private static final int PORT = 8080;

	
	public static void main(String [] args) {
		byte buffer[] = new byte[256]; 
		try(
				DatagramSocket server = new DatagramSocket(PORT)
		) {
			DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
			
			while(true) {
				server.receive(dp);
				
				String date = new Date().toString();
				
				dp = new DatagramPacket(date.getBytes(), date.getBytes().length, dp.getAddress(), dp.getPort());
				server.send(dp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
