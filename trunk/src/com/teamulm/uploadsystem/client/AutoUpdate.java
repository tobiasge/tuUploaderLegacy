package com.teamulm.uploadsystem.client;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

public class AutoUpdate extends Thread {

	private static final String VERSION = "2.10";

	private static final String SERVER = "www.team-ulm.de";

	private static final char commandSep = (char) (0x14);

	private static final String lfcr = "\n";

	private JFrame pbFrame;

	private JLabel pbLabel;

	private GridBagConstraints constrain;

	private BufferedReader input;

	private BufferedWriter output;

	private BufferedInputStream infile;

	private Socket server;

	private InetAddress serverAdress;

	private static final int PORT = 1809;

	private boolean running;

	private boolean connected;

	// private int totalFiles;

	public AutoUpdate() {
		super();
		/** ******************************************************************************** */
		this.pbFrame = new JFrame();
		this.pbFrame.setIconImage(new ImageIcon("icon.gif").getImage());
		this.pbFrame.setTitle("Autoupdate");
		this.pbFrame
				.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.pbFrame.setUndecorated(true);
		this.pbFrame.setResizable(false);
		this.pbFrame.setLayout(new GridBagLayout());
		this.constrain = new GridBagConstraints();
		this.constrain.insets = new Insets(2, 2, 2, 2);
		this.constrain.anchor = GridBagConstraints.NORTH;
		this.constrain.anchor = GridBagConstraints.SOUTHWEST;
		this.constrain.gridy = 1;
		this.pbLabel = new JLabel("Verbinde mit Server");
		this.pbLabel.setFont(new Font("", Font.BOLD, 12));
		this.pbFrame.add(this.pbLabel, this.constrain);
		this.pbFrame.setSize(300, 30);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.pbFrame.setLocation((d.width - this.pbFrame.getSize().width) / 2,
				(d.height - this.pbFrame.getSize().height) / 2);
		this.pbFrame.setVisible(true);
		/** ******************************************************************************** */
		this.running = true;
		try {
			this.serverAdress = InetAddress.getByName(AutoUpdate.SERVER);
			this.server = new Socket(this.serverAdress, AutoUpdate.PORT);
			// Time out in MilliSec - 3 min
			this.server.setSoTimeout(1000 * 60 * 3);
			this.infile = new BufferedInputStream(this.server.getInputStream());
			this.input = new BufferedReader(new InputStreamReader(this.server
					.getInputStream(), Charset.forName("UTF-8")));
			this.output = new BufferedWriter(new OutputStreamWriter(this.server
					.getOutputStream(), Charset.forName("UTF-8")));
			this.connected = true;
		} catch (ConnectException connExc) {
			this.pbLabel.setText("Server nicht gefunden.");
			this.connected = false;
		} catch (Exception e) {
			e.printStackTrace();
			this.connected = false;
		}
	}

	@Override
	public void run() {
		int filesRead = 0;
		try {
			if (!this.connected) {
				Thread.sleep(100);
				return;
			}
			this.pbLabel.setText("Überprüfe Version");
			this.output.write("VERSION" + AutoUpdate.commandSep
					+ AutoUpdate.VERSION + AutoUpdate.lfcr);
			this.output.flush();
			String reply = this.input.readLine();
			if (reply.endsWith("NEED")) {
				this.output.write("GET" + AutoUpdate.commandSep
						+ AutoUpdate.lfcr);
				this.output.flush();
			} else {
				this.output.write("BYE" + AutoUpdate.commandSep
						+ AutoUpdate.lfcr);
				this.output.flush();
				this.running = false;
				this.close();
			}
			while (this.running) {
				System.out.println("Lese command");
				String command = this.input.readLine();
				System.out.println("command gelesen : " + command);
				String[] commandParts = command.split(""
						+ AutoUpdate.commandSep);
				if (commandParts[0].equalsIgnoreCase("FILE"))
					if (commandParts[1].equalsIgnoreCase("BAD")) {
						this.running = false;
						this.output.write("BYE" + AutoUpdate.commandSep
								+ AutoUpdate.lfcr);
						this.output.flush();
						this.close();
						// TeamUlmUpload.getInstance().setUpdated(
						// this.totalFiles == filesRead);
					} else {
						this.pbLabel.setText("Lese: " + commandParts[1]);
						boolean fileOK = this.saveFile(commandParts[1], Long
								.parseLong(commandParts[2]));
						if (fileOK) {
							filesRead++;
							this.output.write("FILE" + AutoUpdate.commandSep
									+ "OK" + AutoUpdate.lfcr);
							this.output.flush();
						} else {
							System.out.println("Sendig file bad");
							this.output.write("FILE" + AutoUpdate.commandSep
									+ "BAD" + AutoUpdate.lfcr);
							this.output.flush();
						}
					}
				else if (commandParts[0].equalsIgnoreCase("FILES")) {
					// this.totalFiles = Integer.parseInt(commandParts[1]);
				} else if (commandParts[0].equalsIgnoreCase("DONE")) {
					System.out.println("Beende wegen DONE");
					this.output.write("BYE" + AutoUpdate.commandSep
							+ AutoUpdate.lfcr);
					this.output.flush();
					this.close();
					// TeamUlmUpload.getInstance().setUpdated(
					// this.totalFiles == filesRead);
					Thread.sleep(1000);
					this.running = false;
				} else {
					System.out.println("else Fall");
					System.out.println(command);
				}
			}
		} catch (Exception e) {
			this.running = false;
			e.printStackTrace();
		} finally {
			this.running = false;
			this.pbFrame.dispose();
		}
	}

	private synchronized boolean saveFile(String name, long length) {
		File target = new File("./" + name);
		new File(target.getParent()).mkdirs();
		long finallength = 0;
		byte[] buffer = new byte[256];
		try {
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(target)));
			int len = 0;
			while ((finallength < length)
					&& ((len = this.infile.read(buffer)) != -1)) {
				finallength += len;
				if (finallength > length)
					return false;
				System.out
						.println("Gelesen: " + finallength + " von " + length);
				out.write(buffer, 0, len);
				out.flush();
			}
			out.flush();
			out.close();
			return finallength == length;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void close() {
		try {
			this.infile.close();
			this.input.close();
			this.output.close();
			this.infile = null;
			this.input = null;
			this.output = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean isRunning() {
		return this.running;
	}
}
