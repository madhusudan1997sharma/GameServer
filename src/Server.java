import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import UDP.UDPServer;

public class Server {
	
	public static volatile Vector<User> usersList = new Vector<>();
	public static volatile List<Integer> matchmakingPool = new ArrayList<Integer>();
	
	public static void main(String[] args) throws Exception {
		ServerSocket ss = new ServerSocket(1990);
		Socket socket = null;
		InputStream in = null;
		OutputStream out = null;
		int i = 1;
		
		while(true)
		{
			try {
				socket = ss.accept();
				
				in = socket.getInputStream();
				out = socket.getOutputStream();

				// STARTING NEW DEDICATED THREAD
				User user = new User(i, socket.getInetAddress().toString(), in, out);
				usersList.add(user);
				user.start();
				
			} catch (Exception e) { ss.close(); e.printStackTrace(); }
			i++;
		}
	}
}

class User extends Thread {
	int uid = -1;
	int opponentUid = -1;
	String ipAddress = null;
	byte[] buff = new byte[1024];
	String data = null;
	InputStream in;
	OutputStream out;
	User(int uid, String ipAddress, InputStream in, OutputStream out) throws Exception
	{
		this.uid = uid;
		this.ipAddress = ipAddress.replace("/", "");
		this.in = in;
		this.out = out;

		in.read(buff, 0, buff.length);
		this.data = new String(buff).trim();
	}
	
	public void run()
	{
		System.out.println("User Id: " + uid + "\nIP Address: " + ipAddress + "\nHandshake Data: " + data);
		/*try {
			out.write(("uid#" + String.valueOf(uid)).getBytes(), 0, ("uid#" + String.valueOf(uid)).getBytes().length);
		} catch (Exception e1) { System.out.println("Client Error"); }*/
		
		while(true)
		{	
			if(Thread.interrupted())
				break;
			try {
				buff = new byte[1024];
				//in.read(buff, 0, buff.length);
			    readInputStreamWithTimeout(in, buff, 1 * 60 * 1000);
				data = new String(buff).trim();
				System.out.println("Client " + uid + ": " + data);
				
				String[] vars = data.split("#");		// STRING SPLITTER TO GET THE DESTINATION IP
				if(vars[0].equals("requestId"))
					out.write(("uid#" + String.valueOf(uid)).getBytes(), 0, ("uid#" + String.valueOf(uid)).getBytes().length);
				else if(vars[0].equals("matchmaking"))
				{
					if(Server.matchmakingPool.isEmpty())
					{	
						Server.matchmakingPool.add(uid);
						out.write("Created Room".getBytes(), 0, "Created Room".getBytes().length);
					}
					else {
						for(User client : Server.usersList)
						{
							if(client.uid == Server.matchmakingPool.get(0) && client.opponentUid == -1 && client.uid != this.uid)
							{
								client.opponentUid = this.uid;
								this.opponentUid = client.uid;
								out.write(("matched#" + String.valueOf(client.uid)).getBytes(), 0, ("matched#" + String.valueOf(client.uid)).getBytes().length);
								client.out.write(("matched#" + String.valueOf(this.uid)).getBytes(), 0, ("matched#" + String.valueOf(this.uid)).getBytes().length);
								break;
							}
						}
					}
				}
				else if(vars[0].equals("message"))
				{
					for(User client : Server.usersList)
					{
						if(this.opponentUid == client.uid)
						{
							client.out.write(data.getBytes(), 0, data.getBytes().length);
							break;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("Client " + uid + " Disconnected");
				Server.usersList.remove((User)this);
				e.printStackTrace();
				break;
			}
		}
	}
	
	void readInputStreamWithTimeout(InputStream is, byte[] b, int timeoutMillis) throws Exception  {
	     long maxTimeMillis = System.currentTimeMillis() + timeoutMillis;
	     while (System.currentTimeMillis() < maxTimeMillis) {
	         int readLength = java.lang.Math.min(is.available(), b.length - 0);
	         is.read(b, 0, readLength);
	         if (readLength != 0)
	        	 return;
	     }
	     
	     Thread.currentThread().interrupt();
	 }
}