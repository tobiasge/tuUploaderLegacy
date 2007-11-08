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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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

	public void engineInit(File[] list, String user, String password) {
		this.trmEngine = new TrmEngine(list);
		this.trmEngine.setUserPass(user, password);
	}

	public void engineStart() {
		this.trmEngine.start();
	}

	public void engineKill() {
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
				Properties mailProperties = new Properties();
				mailProperties.setProperty("mail.smtp.host",
						"hermes.nb.team-ulm.de");
				Session mailSession = Session
						.getDefaultInstance(mailProperties);
				MimeMessage mailMessage = new MimeMessage(mailSession);
				InternetAddress from = new InternetAddress();
				from.setAddress("uploaderror@team-ulm.de");
				from.setPersonal("Upload Error");
				InternetAddress to = new InternetAddress();
				to.setAddress("tobias.genannt@team-ulm.de");
				to.setPersonal("Tobias Genannt");
				mailMessage.addFrom(new Address[] { from });
				mailMessage.addRecipient(Message.RecipientType.TO, to);
				mailMessage.setSubject("Error: " + error.getClass());
				mailMessage.setText(stackTrace + "\n\nLogfile:" + report,
						"UTF-8", "plain");
				mailMessage.setHeader("X-Mailer", "TU-Uploader");
				mailMessage.setSentDate(new Date());
				Transport.send(mailMessage);
			} catch (Exception e) {
				System.out.println(e.getClass() + ": " + e.getMessage());
				JOptionPane.showMessageDialog(null,
						"Bericht konnte nicht gesendet werden.", "Fehler...",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
