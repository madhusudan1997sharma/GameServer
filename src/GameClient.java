import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.net.*;

public class GameClient {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Socket socket;
		try {
			socket = new Socket(InetAddress.getByName("127.0.0.1"), 1990);
			DataOutputStream s = new DataOutputStream(socket.getOutputStream());
			s.writeUTF("Hello");
			s.flush();
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}