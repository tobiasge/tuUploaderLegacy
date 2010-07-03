package com.teamulm.uploadsystem.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
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
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;

public class Helper {

	private static Helper instance;

	private static final Logger log = Logger.getLogger(Helper.class);

	public static Helper getInstance() {
		if (null == Helper.instance) {
			Helper.instance = new Helper();
		}
		return Helper.instance;
	}

	public void copyFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException ioException) {
			throw ioException;
		} finally {
			if (inChannel != null) {
				inChannel.close();
			}
			if (outChannel != null) {
				outChannel.close();
			}
		}
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
			log.error("could not read file: " + fileName); //$NON-NLS-1$
			return null;
		}
		Object[] list = listData.toArray();
		String[] ret = new String[list.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String) list[i];
		return ret;
	}

	public void systemCrashHandler(final Exception error) {
		log.error("", error); //$NON-NLS-1$
		Display.getDefault().asyncExec(new Runnable() {

			public void run() {

				MessageBox mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.YES | SWT.NO);
				mb.setText(Messages.getString("Helper.title.errorReport")); //$NON-NLS-1$
				mb.setMessage(Messages.getString("Helper.msg.errorReport")); //$NON-NLS-1$

				if (SWT.YES != mb.open()) {
					return;
				}

				StringWriter sw = new StringWriter();
				error.printStackTrace(new PrintWriter(sw));
				String stackTrace = sw.toString();

				if (null != TeamUlmUpload.getInstance() && null != TeamUlmUpload.getInstance().getMainWindow()) {
					TeamUlmUpload.getInstance().getMainWindow()
						.addStatusLine(Messages.getString("Helper.logMessages.sendreport")); //$NON-NLS-1$
				}
				String[] lines = Helper.this.readFileData(TeamUlmUpload.logFileName, false);
				String report = ""; //$NON-NLS-1$
				if (null != lines) {
					for (String line : lines) {
						report += line + "\n"; //$NON-NLS-1$
					}
				} else {
					report = "Konnte Log nicht lesen"; //$NON-NLS-1$
				}
				try {
					Properties mailProperties = new Properties();
					mailProperties.setProperty("mail.smtp.host", "hermes.nb.team-ulm.de"); //$NON-NLS-1$ //$NON-NLS-2$
					Session mailSession = Session.getDefaultInstance(mailProperties);
					MimeMessage mailMessage = new MimeMessage(mailSession);
					InternetAddress from = new InternetAddress();
					from.setAddress("uploaderror@team-ulm.de"); //$NON-NLS-1$
					from.setPersonal(TrmEngine.getInstance().getUserName());
					InternetAddress to = new InternetAddress();
					to.setAddress("tobias.genannt@team-ulm.de"); //$NON-NLS-1$
					to.setPersonal("Tobias Genannt"); //$NON-NLS-1$
					mailMessage.addFrom(new Address[] { from });
					mailMessage.addRecipient(Message.RecipientType.TO, to);
					mailMessage.setSubject("Error: " + error.getClass()); //$NON-NLS-1$
					mailMessage.setText(stackTrace + "\n\nLogfile:" + report, "UTF-8", "plain"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					mailMessage.setHeader("X-Mailer", "TU-Uploader"); //$NON-NLS-1$ //$NON-NLS-2$
					mailMessage.setSentDate(new Date());
					Transport.send(mailMessage);
				} catch (Exception e) {
					mb = new MessageBox(Display.getDefault().getActiveShell(), SWT.OK);
					mb.setText(Messages.getString("String.error")); //$NON-NLS-1$
					mb.setMessage(Messages.getString("Helper.msg.errorNotSend")); //$NON-NLS-1$
					mb.open();
				}
			}
		});
	}
}
