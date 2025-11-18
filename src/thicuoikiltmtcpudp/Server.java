package thicuoikiltmtcpudp;

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class Server {
	public static List<InetAddress> blackList = new ArrayList<InetAddress>();

	public static void main(String[] args) {

		try {
			blackList.add(InetAddress.getByName("192.168.10.100"));
			blackList.add(InetAddress.getByName("192.168.10.102"));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		new CaptureThread().start();

		try (ServerSocket serverSocket = new ServerSocket(8888)) {
			while (true) {
				Socket socket = serverSocket.accept();
				InetAddress clientAddress = socket.getInetAddress();
				boolean check = false;
				for (InetAddress address : blackList) {
					if (address.equals(clientAddress)) {
						check = true;
						break;
					}
				}
				if (!check)
					new ClientProcessing(socket, new DataInputStream(socket.getInputStream()),
							new DataOutputStream(socket.getOutputStream())).start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

class CaptureThread extends Thread {
	public static byte data[];

	@Override
	public void run() {
		Robot r = null;
		try {
			r = new Robot();
		} catch (Exception ex) {
			return;
		}
		Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
		while (true) {
			try {
				BufferedImage img = r.createScreenCapture(capture);
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				data = baos.toByteArray();
				Thread.sleep(2);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}

class ClientProcessing extends Thread {

	Socket socket;
	DataInputStream dis;
	DataOutputStream dos;

	public ClientProcessing(Socket socket, DataInputStream dis, DataOutputStream dos) {
		this.socket = socket;
		this.dis = dis;
		this.dos = dos;
	}

	@Override
	public void run() {
		try {
			String msg = dis.readUTF();
			if (msg.equals("Please livestream")) {
				dos.writeUTF("OK");
				while (true) {
					try {
						msg = dis.readUTF();
						if (msg.equals("Get Video")) {
							if (CaptureThread.data != null) {
								byte tmp[] = CaptureThread.data;

								dos.writeInt(tmp.length);
								dos.write(tmp);
								dos.flush();
							}
							Thread.sleep(2);
						} else {
							JOptionPane.showMessageDialog(null,
									"Không phải GetVideo" + socket.getLocalPort() + " " + socket.getInetAddress(),
									"Thông báo", JOptionPane.INFORMATION_MESSAGE);
							return;
						}
					} catch (IOException e) {
						JOptionPane.showMessageDialog(null,
								"Client ngắt kết nối: " + socket.getLocalPort() + " " + socket.getInetAddress(),
								"Thông báo", JOptionPane.INFORMATION_MESSAGE);
						return;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			JOptionPane.showMessageDialog(null,
					"Client ngắt kết nối: " + socket.getLocalPort() + " " + socket.getInetAddress(), "Thông báo",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

	}
}