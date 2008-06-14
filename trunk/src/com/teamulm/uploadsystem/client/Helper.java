package com.teamulm.uploadsystem.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.gui.MainWindow;

public class Helper {

	private static final Logger log = Logger.getLogger(Helper.class);

	private static Helper instance;

	public static Helper getInstance() {
		if (null == Helper.instance) {
			Helper.instance = new Helper();
		}
		return Helper.instance;
	}

	public String getFileLocation(String fileName) {
		return TeamUlmUpload.getAppDataDir() + fileName;
	}

	public String[] readLocFile(String fileName, boolean reportError) {
		ArrayList<String> listData = new ArrayList<String>();
		BufferedReader inputStream = null;
		String inDataStr = null;
		try {
			inputStream = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			while ((inDataStr = inputStream.readLine()) != null)
				listData.add(inDataStr);
			inputStream.close();
		} catch (Exception ex) {
			log.error("could not read locations file: " + fileName);
			return new String[] { "Fehler, bitte Locations", "Update durchführen" };
		}
		Object[] list = listData.toArray();
		String[] ret = new String[list.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String) list[i];
		return ret;
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
			String[] lines = this.readLocFile(TeamUlmUpload.LOGFILE, false);
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
