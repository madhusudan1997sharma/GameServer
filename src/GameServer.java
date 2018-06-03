import java.net.*;

public class GameServer {
	
	public static void main(String[] args) {
		GameServer s = new GameServer(1990);
		s.start();
	}

	DatagramSocket socket = null;
	public GameServer(int port)
	{
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void start()
	{
		Thread t = new Thread(() -> listen());
		t.start();
	}
	
	public void listen()
	{
		byte[] data = new byte[1024];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		int i = 1;
		while(true)
		{
			try {
				int j = i;
				socket.receive(packet);
				Thread t = new Thread(() -> user(j));
				t.start();
				i++;
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	public void user(int number)
	{
		while(true)
		{	
			System.out.println("User: " + number);
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) { }
		}
	}

}