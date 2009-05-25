package com.teamulm.uploadsystem.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

import com.teamulm.uploadsystem.client.gui.Messages;

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

	public String[] readFileData(String fileName, boolean reportError) {
		ArrayList<String> listData = new ArrayList<String>();
		BufferedReader inputStream = null;
		String inDataStr = null;
		try {
			inputStream = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
			while ((inDataStr = inputStream.readLine()) != null)
				listData.add(inDataStr);
			inputStream.close();
		} catch (Exception ex) {
			log.error("could not read file: " + fileName);
			return null;
		}
		Object[] list = listData.toArray();
		String[] ret = new String[list.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String) list[i];
		return ret;
	}

	public String[] readFileData(byte[] data, boolean reportError) {
		ArrayList<String> listData = new ArrayList<String>();
		BufferedReader inputStream = null;
		String inDataStr = null;
		try {
			inputStream = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(data)));
			while ((inDataStr = inputStream.readLine()) != null)
				listData.add(inDataStr);
			inputStream.close();
		} catch (Exception ex) {
			return null;
		}
		Object[] list = listData.toArray();
		String[] ret = new String[list.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String) list[i];
		return ret;
	}

	public void systemCrashHandler(final Exception error) {
		log.error("", error);
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {
				StringWriter sw = new StringWriter();
				error.printStackTrace(new PrintWriter(sw));
				String stackTrace = sw.toString();

				MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.YES | SWT.NO);
				mb.setText(Messages.getString("Helper.title.errorReport"));
				mb.setMessage(Messages.getString("Helper.msg.errorReport"));

				if (SWT.YES != mb.open())
					return;
				else {
					if (null != TeamUlmUpload.getInstance() && null != TeamUlmUpload.getInstance().getMainWindow()) {
						TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
							Messages.getString("Helper.logMessages.sendreport"));
					}
					String[] lines = Helper.this.readFileData(TeamUlmUpload.logFileName, false);
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
						mailProperties.setProperty("mail.smtp.host", "hermes.nb.team-ulm.de");
						Session mailSession = Session.getDefaultInstance(mailProperties);
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
						mailMessage.setText(stackTrace + "\n\nLogfile:" + report, "UTF-8", "plain");
						mailMessage.setHeader("X-Mailer", "TU-Uploader");
						mailMessage.setSentDate(new Date());
						Transport.send(mailMessage);
					} catch (Exception e) {
						System.out.println(e.getClass() + ": " + e.getMessage());
						mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.YES | SWT.NO);
						mb.setText(Messages.getString("String.error"));
						mb.setMessage(Messages.getString("Helper.msg.errorNotSend"));
						mb.open();
					}
				}
			}
		});
	}
}
