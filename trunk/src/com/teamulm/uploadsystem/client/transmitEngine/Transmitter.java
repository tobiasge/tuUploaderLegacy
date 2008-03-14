package com.teamulm.uploadsystem.client.transmitEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.layout.MainWindow;
import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.protocol.Command;
import com.teamulm.uploadsystem.protocol.GetGalleriesCmd;
import com.teamulm.uploadsystem.protocol.HelloCmd;
import com.teamulm.uploadsystem.protocol.LockPathCmd;
import com.teamulm.uploadsystem.protocol.LoginCmd;
import com.teamulm.uploadsystem.protocol.PingCmd;
import com.teamulm.uploadsystem.protocol.QuitCmd;
import com.teamulm.uploadsystem.protocol.SaveFileCmd;
import com.teamulm.uploadsystem.protocol.SaveGalleryCmd;
import com.teamulm.uploadsystem.protocol.UnLockPathCmd;

public class Transmitter extends Thread {

	private static final Logger log = Logger.getLogger(Transmitter.class);

	private TrmEngine chef;

	private File akt;

	private Socket server;

	private InetAddress serverAdress;

	private ObjectInputStream input;

	private ObjectOutputStream output;

	private boolean connected;

	private boolean Running;

	private Gallery gallery;

	private Timer keepAliveTimer;

	public Transmitter(TrmEngine chef) {
		super();
		this.setName("Transmitter");
		this.chef = chef;
		this.Running = true;
		this.gallery = this.getGalleryData();
		try {
			int serverPort = Integer.parseInt(TeamUlmUpload.getInstance()
					.getClientConf().getProperty("serverPort"));
			String serverName = TeamUlmUpload.getInstance().getClientConf()
					.getProperty("serverName", "tmp");
			this.serverAdress = InetAddress.getByName(serverName);
			this.server = new Socket(this.serverAdress, serverPort);
			if (this.server.isConnected()) {
				this.output = new ObjectOutputStream(this.server
						.getOutputStream());
				this.input = new ObjectInputStream(this.server.getInputStream());
				this.connected = true;
			}
		} catch (Exception e) {
			MainWindow.getInstance().addStatusLine(
					"Konnte Verbindung nicht herstellen");
			this.connected = false;
			Helper.getInstance().systemCrashHandler(e);
		}
		this.keepAliveTimer = new Timer("KeepAliveTimer", true);
		this.keepAliveTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				Transmitter.this.sendAndReadCommand(new PingCmd());
			}
		}, 0, 2000);
	}

	private Gallery getGalleryData() {
		Gallery gallery = new Gallery();
		gallery.setDesc(MainWindow.getInstance().getEventDesc());
		gallery.setIntern(MainWindow.getInstance().getIntern());
		gallery.setTitle(MainWindow.getInstance().getEventTitle());
		gallery.setLocation(MainWindow.getInstance().getLocations()
				.getSelectedLoc());
		gallery.setDate(MainWindow.getInstance().getDateEditor()
				.getDateString());
		return gallery;
	}

	private synchronized Command sendAndReadCommand(Command command) {
		try {
			this.output.writeObject(command);
			this.output.flush();
			Command retVal = (Command) this.input.readObject();
			return retVal;
		} catch (ClassCastException e) {
			Helper.getInstance().systemCrashHandler(e);
		} catch (ClassNotFoundException e) {
			Helper.getInstance().systemCrashHandler(e);
		} catch (IOException e) {
			Helper.getInstance().systemCrashHandler(e);
		}
		return null;
	}

	public synchronized boolean isConnected() {
		return this.connected;
	}

	public synchronized boolean login(String username, String passwd) {
		LoginCmd cmd = new LoginCmd();
		cmd.setUserName(username);
		cmd.setPassWord(passwd);
		try {
			Command retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			return retVal instanceof LoginCmd && retVal.commandSucceded();
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	public synchronized boolean verCheck() {
		HelloCmd cmd = new HelloCmd();
		cmd.setProtocolVersionString(TrmEngine.VERSION);
		try {
			Command retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			return retVal instanceof HelloCmd && retVal.commandSucceded();
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	public synchronized boolean setLocation() {
		try {
			LockPathCmd cmd = new LockPathCmd();
			cmd.setDate(this.gallery.getDate());
			cmd.setLocation(this.gallery.getLocation());
			Command retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			if (!(retVal instanceof LockPathCmd))
				return false;
			LockPathCmd resp = (LockPathCmd) retVal;
			if (resp.commandSucceded()) {
				if (resp.getStartNumber() > 1) {
					this.gallery.setNewGallery(false);
				}
				this.chef.setStartNumber(resp.getStartNumber());
				return true;
			} else if (resp.getErrorCode() == Command.ERROR_LOC_BADLOC) {
				MainWindow.getInstance()
						.addStatusLine("Location nicht gültig.");
				return false;
			} else if (resp.getErrorCode() == Command.ERROR_LOC_NOTFREE) {
				MainWindow.getInstance()
						.addStatusLine("Location in Benutzung.");
				MainWindow.getInstance().addStatusLine(
						"Bitte später nochmal probieren.");
				return false;
			} else
				return false;
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	protected synchronized ArrayList<Gallery> getGalleriesFor(String date) {
		GetGalleriesCmd cmd = new GetGalleriesCmd();
		cmd.setDate(date);
		try {
			Command retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			if (retVal instanceof GetGalleriesCmd) {
				GetGalleriesCmd response = (GetGalleriesCmd) retVal;
				return response.getGalleries();
			} else {
				return new ArrayList<Gallery>();
			}
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return new ArrayList<Gallery>();
		}
	}
	
	private byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);
		long length = file.length();
		if (length > Integer.MAX_VALUE) {
			log.error("File is too large to process");
			return null;
		}
		byte[] bytes = new byte[(int) length];
		int offset = 0;
		int numRead = 0;
		while ((offset < bytes.length)
				&& ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "
					+ file.getName());
		}
		is.close();
		return bytes;
	}

	public synchronized void stopIT() {
		this.Running = false;
	}

	protected void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}
	
	protected boolean unLockLocation(Gallery gal) {
		try {
			UnLockPathCmd cmd = new UnLockPathCmd();
			cmd.setDate(gal.getDate());
			cmd.setLocation(gal.getLocation());
			cmd.setSuffix(gal.getSuffix());
			Command retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			if (!(retVal instanceof UnLockPathCmd))
				return false;
			UnLockPathCmd resp = (UnLockPathCmd) retVal;
			return resp.commandSucceded();
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}
	
	public synchronized boolean lockLocation(Gallery gal) {
		try {
			LockPathCmd cmd = new LockPathCmd();
			cmd.setDate(gal.getDate());
			cmd.setLocation(gal.getLocation());
			cmd.setSuffix(gal.getSuffix());
			Command retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			if (!(retVal instanceof LockPathCmd))
				return false;
			LockPathCmd resp = (LockPathCmd) retVal;
			if (resp.commandSucceded()) {
				if (resp.getStartNumber() > 1) {
					this.gallery.setNewGallery(false);
				}
				this.chef.setStartNumber(resp.getStartNumber());
				return true;
			} else if (resp.getErrorCode() == LockPathCmd.ERROR_LOC_BADLOC) {
				return false;
			} else if (resp.getErrorCode() == LockPathCmd.ERROR_LOC_NOTFREE) {
				return false;
			} else
				return false;
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	
	@Override
	public void run() {
		MainWindow.getInstance().addStatusLine("Beginne Übertragung");
		MainWindow.getInstance().setUploadProgress(0);
		Command retVal;
		try {
			while (this.Running && this.chef.isThereSomethingToTtansmit()) {
				if ((this.akt = this.chef.getNextToTransmit()) == null) {
					log.info("this.chef.getNextToTransmit() returned null");
				} else {
					log.info("Sende Datei " + this.akt.getName());
					SaveFileCmd cmd = new SaveFileCmd();
					cmd.setFileName(this.akt.getName());
					cmd.setFileSize((int) this.akt.length());
					cmd.setFileContent(this.getBytesFromFile(this.akt));

					retVal = this.sendAndReadCommand(cmd);
					log.debug("Server said: " + retVal);
					if (retVal instanceof SaveFileCmd
							&& retVal.commandSucceded()) {
						log.info("Datei " + this.akt.getName() + " gesendet");
						this.akt.delete();
					} else {
						log.info("Datei " + this.akt.getName()
								+ " nicht gesendet");
						MainWindow.getInstance().addStatusLine(
								"Konnte " + this.akt.getAbsoluteFile()
										+ " nicht senden");
					}
				}
			}
			SaveGalleryCmd cmd = new SaveGalleryCmd();
			cmd.setGallery(this.gallery);

			retVal = this.sendAndReadCommand(cmd);
			log.debug("Server said: " + retVal);
			if (retVal instanceof SaveGalleryCmd && retVal.commandSucceded()) {
				MainWindow.getInstance().addStatusLine("Galerie gespeichert");
			} else {
				MainWindow.getInstance().addStatusLine(
						"Fehler bei Datenbankeintrag");
			}
			MainWindow.getInstance().addStatusLine("Beende Übertragung");
			this.disconnect();
			MainWindow.getInstance().addStatusLine("Verbindung beendet");
			sleep(10);
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			this.Running = false;
		}
		MainWindow.getInstance().setUploadProgress(1000);
	}

	public synchronized void disconnect() {
		try {
			this.Running = false;
			this.sendAndReadCommand(new QuitCmd());
			this.output.close();
			this.input.close();
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
	}
}
