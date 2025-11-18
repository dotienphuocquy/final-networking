package thicuoikiltmtcpudp;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Client extends JFrame implements Runnable {

	int off = 50;
	BufferedImage img2 = null;

	public Client() {
		this.setTitle("Share Screen");
		this.setSize(500, 400);
		this.setDefaultCloseOperation(3);

		this.setVisible(true);
	}

	@Override
	public void paint(Graphics g) {
		try {
			if (img2 != null) {
				g.drawImage(img2, off, off, this.getWidth() - off, this.getHeight() - off, 0, 0, img2.getWidth(),
						img2.getHeight(), null);
				Thread.sleep(10);
				this.repaint();
			} else
				Thread.sleep(1);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try (Socket socket = new Socket("localhost", 8888);
				DataInputStream dis = new DataInputStream(socket.getInputStream());
				DataOutputStream dos = new DataOutputStream(socket.getOutputStream());) {

			dos.writeUTF("Please livestream");
			socket.setSoTimeout(10000); // doi 10s
			String msg = dis.readUTF();
			if (msg.equals("OK")) {
				while (true) {
					dos.writeUTF("Get Video");
					int len = dis.readInt();
					byte[] tmp = new byte[len];
					dis.readFully(tmp);
					ByteArrayInputStream baos1 = new ByteArrayInputStream(tmp);
					img2 = ImageIO.read(baos1);
				}
			} else {
				System.out.println("Không phải OK");
				return;
			}
		} catch (SocketTimeoutException ste) {
			JOptionPane.showMessageDialog(null, "Đã chờ đợi quá 10s", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

			return;
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null, "Server đã bị sập", "Thông báo", JOptionPane.INFORMATION_MESSAGE);

			return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Thread(new Client()).start();
	}

}
