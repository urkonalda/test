/*
 * Urko Nalda Gil
 * 16626492-E
 */

package net22;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebServer {
	
	private String head;
	private File folder = new File(".");
	
	public static void main(String [] args) {
		new WebServer();
	}
	
	public WebServer() {
		ExecutorService executor = Executors.newCachedThreadPool();
		try(
				ServerSocket ss = new ServerSocket(8080);
		) {

			while(true) {
				Socket s = ss.accept();
				
				Runnable client = new Client(s);
				executor.execute(client);
			}			
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			executor.shutdown();
		}
	}
	
	private String guessContentByExtension(String extension) {
		if (extension == null)
			return "text/plain";
		extension = extension.toLowerCase();
		if (extension.equals("html") || extension.equals("htm")
				|| extension.equals("shtml")) {
			return "text/html";
		} else if (extension.equals("txt")) {
			return "text/plain";
		} else if (extension.equals("gif")) {
			return "image/gif";
		} else if (extension.equals("jpg")) {
			return "image/jpeg";
		} else if (extension.equals("class")) {
			return "application/octet-stream";
		} else if (extension.equals("exe")) {
			return "application/octet-stream";
		} else if (extension.equals("pdf")) {
			return "application/pdf";
		} else if (extension.equals("zip")) {
			return "application/zip";
		} else if (extension.equals("css")) {
			return "text/css";
		} else if (extension.equals("js")) {
			return "application/x-javascript";
		} else if (extension.equals("ico")) {
			return "image/x-icon";
		} else {
			return "text/plain";
		}
	}

	private void sendMIMEHeading(OutputStream os, int code, String cType, long fSize, File f) {
		PrintStream dos = new PrintStream(os);

		SimpleDateFormat sdf = new SimpleDateFormat(
				"EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));

		dos.print("HTTP/1.1 " + code + " ");
		if (code == 200) {
			dos.print("OK\r\n");
			dos.print("Last-Modified: "
					+ sdf.format(new Date(f.lastModified())) + "\r\n");
		} else if (code == 404) {
			dos.print("File Not Found\r\n");
		} else if (code == 501) {
			dos.print("Not Implemented\r\n");
		}
		dos.print("Content-type: " + cType + "\r\n");
		dos.print("Content-length: " + fSize + "\r\n");
		dos.print("Cache-Control: max-age=0\r\n");
		dos.print("Date: " + sdf.format(new Date()) + "\r\n");
		dos.print("Server: SDIS http Server ver. 2.0\r\n");
		dos.print("Connection: close\r\n");
		dos.print("\r\n");
	}

	private String makeHTMLErrorText(int code, String txt) {
		StringBuffer msg = new StringBuffer("<HTML>\r\n");
		msg.append("  <HEAD>\r\n");
		msg.append("    <TITLE>" + txt + "</TITLE>\r\n");
		msg.append("  </HEAD>\r\n");
		msg.append("  <BODY>\r\n");
		msg.append("    <H1>HTTP Error " + code + ": " + txt + "</H1>\r\n");
		msg.append("  </BODY>\r\n");
		msg.append("</HTML>\r\n");
		return msg.toString();
	}
	
	private class Client implements Runnable {
		Socket socket;
		
		Client(Socket socket) {
			this.socket = socket;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void run() {
			try(
					DataInputStream dis = new DataInputStream(socket.getInputStream());
					OutputStream os = socket.getOutputStream();
					PrintStream ps = new PrintStream(os)
			) {
				head = dis.readLine();
				
				if(head == null) {
					return;				
				}
				if(!head.startsWith("GET")) {
					String data = makeHTMLErrorText(501, "Not Implemented");
					sendMIMEHeading(os, 501, "text/html", data.length(), null);
					ps.print(data);
					return;
				}
				
				String line = dis.readLine();
				while(line!=null) {
					if(line.equals("")) {
						break;
					}
					line=dis.readLine();
				}
				
				String resourceName = head.substring(head.indexOf(" ")+1, head.lastIndexOf(" "));
				
				if(resourceName.endsWith("/")) {
					resourceName += "index.html";
				}
				resourceName = resourceName.substring(1);
				File file = new File(folder, resourceName);
				if(!file.exists()) {
					String data = makeHTMLErrorText(404, "File Not Found");
					sendMIMEHeading(os, 404, "text/html", data.length(), null);
					ps.print(data);
					return;
				}
				
				try(
						FileInputStream is = new FileInputStream(resourceName)
				) {
					String extension = file.getName().contains(".") ? file.getName().substring(file.getName().lastIndexOf(".")+1) : null;
					sendMIMEHeading(os, 200, guessContentByExtension(extension), file.length(), file);
					CopyStream.copyStream(is,os);
				} catch(SocketException e) {
					//Catch broken pipes
				}
				
			} catch(IOException e) {
				e.printStackTrace();
			} finally {
				try {
					socket.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
			
		}
		
	}

}
