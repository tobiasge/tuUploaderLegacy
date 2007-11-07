/* TeamUlmUpload.java
 *
 *******************************************************
 *
 * Beschreibung:
 *
 *
 * Autor: Wolfgang Holoch
 * (C) 2004
 *
 *******************************************************/
package com.teamulm.uploadsystem.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.net.Socket;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.teamulm.uploadsystem.client.layout.MainWindow;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;


public class TeamUlmUpload {

	private static final Logger log = Logger.getLogger(TeamUlmUpload.class);

	private TrmEngine trmEngine;

	private static final String CLIENTCONFFILE = "client.conf";

	private static final String LOGFILE = "TeamUlm.log";

	private static TeamUlmUpload instance = null;

	// Konstruktor
	private TeamUlmUpload() {
		OperatingSystemMXBean sysInfo1 = ManagementFactory
				.getOperatingSystemMXBean();
		MemoryMXBean sysInfo2 = ManagementFactory.getMemoryMXBean();
		log.info("Program Startup");
		log.info("Running on: " + sysInfo1.getName() + " "
				+ sysInfo1.getVersion());
		log.info("Systemtype is " + sysInfo1.getArch() + " working on "
				+ sysInfo1.getAvailableProcessors() + " CPU(s)");
		log.info("Memory Usage is: " + sysInfo2.getHeapMemoryUsage());
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				MainWindow.getInstance().populateFields();
			}
		});
	}

	public void EngineInit(File[] list, String user, String password) {
		this.trmEngine = new TrmEngine(list);
		this.trmEngine.setUserPass(user, password);
	}

	public void EngineStart() {
		this.trmEngine.start();
	}

	public void EngineKill() {
		this.trmEngine = null;
	}

	public static TeamUlmUpload getInstance() {
		if (null == TeamUlmUpload.instance)
			TeamUlmUpload.instance = new TeamUlmUpload();
		return TeamUlmUpload.instance;
	}

	public Properties getClientConf() {
		Properties serverConf = new Properties();
		try {
			serverConf.loadFromXML(new FileInputStream(
					TeamUlmUpload.CLIENTCONFFILE));
		} catch (IOException e) {
			TeamUlmUpload.getInstance().systemCrashHandler(e);
		}
		return serverConf;
	}

	public static void main(String[] args) {
		try {
			Properties logConf = new Properties();
			logConf.load(new FileInputStream("client.log4j.properties"));
			PropertyConfigurator.configure(logConf);
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			TeamUlmUpload.getInstance().systemCrashHandler(e);
		}
		System.setProperty("line.separator", "\n");
		TeamUlmUpload.getInstance();
	}

	public void systemCrashHandler(Exception error) {
		log.error("" + error.getMessage());
		StringWriter sw = new StringWriter();
		error.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();

		Object[] options = { "Ja", "Nein" };
		if (JOptionPane.NO_OPTION == JOptionPane
				.showOptionDialog(
						null,
						"Es ist ein Fehler aufgetreten. Soll ein Report erstellt werden?",
						"Fehlerreport...?", JOptionPane.YES_NO_OPTION,
						JOptionPane.ERROR_MESSAGE, null, options, options[1]))
			return;
		else {
			MainWindow.getInstance().addStatusLine("Sende Fehlerbericht");
			String[] lines = Helper.getInstance().readFileData(
					TeamUlmUpload.LOGFILE, false);
			String report = "";
			if (null != lines) {
				for (String line : lines) {
					report += line + "\n";
				}
			} else {
				report = "Konnte Log nicht lesen";
			}
			try {
				Emailer.send("hermes.nb.team-ulm.de", 25,
						"tobias.genannt@team-ulm.de",
						"uploaderror@team-ulm.de",
						"Error: " + error.getClass(), "Stacktrace: "
								+ stackTrace + "\n\nLogfile:" + lines);

			} catch (Exception e) {
				System.out.println(e.getClass() + ": " + e.getMessage());
				JOptionPane.showMessageDialog(null,
						"Bericht konnte nicht gesendet werden.", "Fehler...",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}

class Emailer {

	public static void main(String[] args) throws Exception {
		String results = send("localhost", 25, "sender@somewhere.com",
				"recipient@somewhere.com", "Test Email", "<b>You got mail!</b>");
		System.out.println(results);
	}

	/**
	 * Sends an email.
	 * 
	 * @return The full SMTP conversation as a string.
	 */
	public static String send(String host, int port, String to, String from,
			String subject, String message) throws Exception {

		// Save the SMTP conversation into this buffer (for debugging if
		// necessary)
		StringBuffer buffer = new StringBuffer();

		try {

			// Connect to the SMTP server running on the local machine. Usually
			// this is SendMail
			Socket smtpSocket = new Socket(host, port);

			// We send commands TO the server with this
			DataOutputStream output = new DataOutputStream(smtpSocket
					.getOutputStream());

			// And recieve responses FROM the server with this
			BufferedReader input = new BufferedReader(new InputStreamReader(
					new DataInputStream(smtpSocket.getInputStream())));

			try {

				// Read the server's hello message
				read(input, buffer);

				// Say hello to the server
				send(output, "HELO localhost.localdomain\r\n", buffer);
				read(input, buffer);

				// Who is sending the email
				send(output, "MAIL FROM: <" + from + ">\r\n", buffer);
				read(input, buffer);

				// Where the mail is going
				send(output, "RCPT to: <" + to + ">\r\n", buffer);
				read(input, buffer);

				// Start the message
				send(output, "DATA\r\n", buffer);
				read(input, buffer);

				// Set the subject
				send(output, "Subject: " + subject + "\r\n", buffer);

				// If we detect HTML in the message, set the content type so it
				// displays
				// properly in the recipient's email client.
				if (message.indexOf("<") == -1) {
					send(
							output,
							"Content-type: text/plain; charset=\"us-ascii\"\r\n",
							buffer);
				} else {
					send(
							output,
							"Content-type: text/html; charset=\"us-ascii\"\r\n",
							buffer);
				}

				// Send the message
				send(output, message, buffer);

				// Finish the message
				send(output, "\r\n.\r\n", buffer);
				read(input, buffer);
				// Close the socket
				smtpSocket.close();

			} catch (IOException e) {
				System.out.println("Cannot send email as an error occurred.");
			}
		} catch (Exception e) {
			System.out.println("Host unknown");
		}

		return buffer.toString();

	}

	/**
	 * Sends a message to the server using the DataOutputStream's writeBytes()
	 * method. Saves what was sent to the buffer so we can record the
	 * conversation.
	 */
	private static void send(DataOutputStream output, String data,
			StringBuffer buffer) throws IOException {
		output.writeBytes(data);
		buffer.append(data);
	}

	/**
	 * Reads a line from the server and adds it onto the conversation buffer.
	 */
	private static void read(BufferedReader br, StringBuffer buffer)
			throws IOException {
		int c;
		while ((c = br.read()) != -1) {
			buffer.append((char) c);
			if (c == '\n') {
				break;
			}
		}
	}

}
