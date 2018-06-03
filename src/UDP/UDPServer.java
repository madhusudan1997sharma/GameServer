package UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class UDPServer {
	
	public static volatile Vector<User> usersList = new Vector<>();
	public static volatile List<Integer> matchmakingPool = new ArrayList<Integer>();
	static int userCount = 1;
	static DatagramSocket socket;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			socket = new DatagramSocket(1990);
			
			byte[] receive = new byte[1024];
			DatagramPacket packet = null;
			while(true)
			{
				packet = new DatagramPacket(receive, receive.length);
				socket.receive(packet);
				
				StringBuilder sb = new StringBuilder();
				int i = 0;
				while(receive[i] != 0)
				{
					sb.append((char)receive[i]);
					i++;
				}
				System.out.println(packet.getAddress().toString().replace("/", "") + ": " + sb.toString());
				if(sb.toString().equals("login"))
				{
					User user = new User(userCount, packet.getAddress().toString().replace("/", ""));
					usersList.add(user);
					user.start();
					userCount ++;
				}
				else
				{
					for(User client : UDPServer.usersList)
					{
						if(client.uid == Integer.parseInt(sb.toString().split("#")[0]))
						{
							client.handler(sb.toString());
							break;
						}
					}
				}
				
				receive = new byte[1024];
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}

class User extends Thread {
	int uid = -1, opponentUid = -1;
	String ipAddress = null, opponentIpAddress = null;
	String[] temp;
	User(int uid, String ipAddress)
	{
		this.uid = uid;
		this.ipAddress = ipAddress;
	}
	
	public void run() {
		try {
			send(ipAddress, "loggedIn#" + uid);
			System.out.println(uid);
		} catch (Exception e) { e.printStackTrace(); }
	}
	
	public void handler(String data)
	{
		temp = data.split("#");
		if(temp[1].equals("matchmaking"))
		{
			if(UDPServer.matchmakingPool.isEmpty())
				UDPServer.matchmakingPool.add(uid);
			else
			{
				for(User client : UDPServer.usersList)
				{
					if(client.uid == UDPServer.matchmakingPool.get(0) && client.uid != uid)
					{
						UDPServer.matchmakingPool.remove(0);
						client.opponentUid = uid;
						client.opponentIpAddress = ipAddress;
						opponentUid = client.uid;
						opponentIpAddress = client.ipAddress;
						send(ipAddress, "matched#" + opponentUid);
						send(client.ipAddress, "matched#" + client.opponentUid);
						break;
					}
				}
			}
		}
		else if(temp[1].equals("message"))
		{
			send(opponentIpAddress, "message#" + temp[2]);
		}
	}
	
	void send(String ipAddress, String data)
	{
			try {
				//for(int i = 0; i < 10; i++)
					UDPServer.socket.send(new DatagramPacket(data.getBytes(), data.getBytes().length, InetAddress.getByName(ipAddress), 1991));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
}