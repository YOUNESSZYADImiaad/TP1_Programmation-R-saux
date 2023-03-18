package blocking;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiThreadChatServer extends Thread{
	private List<Conversation> conversations = new ArrayList();
	int clientcount;
	public static void main(String[] args) {
		new MultiThreadChatServer().start();

	}
	
	
	@Override
	public void run() {
		System.out.print("The Server is Start in Port : 1234");
		try {
			ServerSocket ss = new ServerSocket(1234);
			while (true) {
				Socket socket = ss.accept();
				++clientcount;
				Conversation conversation= new Conversation(socket,clientcount);
				conversations.add(conversation);
				conversation.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	class Conversation extends Thread{
		private Socket socket;
		private int clientid;
		public Conversation(Socket socket,int id) {
			this.socket=socket;
			this.clientid=id;
		}
		@Override
		public void run() {
			try {
				InputStream is = socket.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				OutputStream os = socket.getOutputStream();
				PrintWriter pw = new PrintWriter(os,true);
				String IP = socket.getRemoteSocketAddress().toString();
				System.out.print("New Client Connnecting => id : "+clientid+" IP : "+IP);
				pw.println("Welcome Your ID : "+clientid);
				String req;
				while((req=br.readLine())!=null) {
					System.out.print(" IP : "+IP+" Request : "+req);
					List<Integer> clientsTo = new ArrayList<>();
					String message;
					if(req.contains("=>")) {
						String[] items=req.split("=>");
						String Clients = items[0];
						message = items[1];
								if(Clients.contains(",")) {
									String[] clientsIds = Clients.split(",");
									for(String id:clientsIds) {
										clientsTo.add(Integer.parseInt(id));
									}
								}else {
									clientsTo.add(Integer.parseInt(Clients));
								}
					}else {
						clientsTo=conversations.stream().map(c->c.clientid).collect(Collectors.toList());
						message=req;
					}
					
					boradcastmessage(req,this,clientsTo);
					
				}
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	public void boradcastmessage(String message, Conversation from,List<Integer> clients) {
		try {
		for(Conversation conversation:conversations) {
			if(conversation != from && clients.contains(conversation.clientid)) {
				Socket socket = conversation.socket;
				OutputStream os = socket.getOutputStream();
				PrintWriter pw = new PrintWriter(os,true);
				pw.println(message);
				System.out.println("Broadcast message: " + message);  // <-- print the message being broadcasted
			}
			
			
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
