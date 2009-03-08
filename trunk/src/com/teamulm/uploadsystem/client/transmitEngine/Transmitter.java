package com.teamulm.uploadsystem.client.transmitEngine;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.Location;
import com.teamulm.uploadsystem.exception.AuthenticationException;
import com.teamulm.uploadsystem.protocol.AuthenticationCmd;
import com.teamulm.uploadsystem.protocol.Command;
import com.teamulm.uploadsystem.protocol.GetGalleriesCmd;
import com.teamulm.uploadsystem.protocol.GetLocationsCmd;
import com.teamulm.uploadsystem.protocol.HelloCmd;
import com.teamulm.uploadsystem.protocol.LockPathCmd;
import com.teamulm.uploadsystem.protocol.LoginCmd;
import com.teamulm.uploadsystem.protocol.NewGalleryCmd;
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

	private boolean Running, loggedIn;

	private Gallery gallery;

	private Timer keepAliveTimer;

	public Transmitter(TrmEngine chef) {
		super();
		this.setName("Transmitter");
		this.chef = chef;
		this.Running = true;
		this.loggedIn = false;
		try {
			int serverPort = Integer.parseInt(TeamUlmUpload.getInstance().getClientConf().getProperty("serverPort"));
			String serverName = TeamUlmUpload.getInstance().getClientConf().getProperty("serverName", "tmp");
			this.serverAdress = InetAddress.getByName(serverName);
			this.server = new Socket(this.serverAdress, serverPort);
			if (this.server.isConnected()) {
				this.output = new ObjectOutputStream(this.server.getOutputStream());
				this.input = new ObjectInputStream(this.server.getInputStream());
				this.connected = true;
			}
		} catch (Exception e) {
			this.connected = false;
			Helper.getInstance().systemCrashHandler(e);
		}
		this.keepAliveTimer = new Timer("KeepAliveTimer", true);
		this.keepAliveTimer.schedule(new KeepAliveTimerTask(), 0, 1000 * 60 * 2);
	}

	private synchronized Command sendAndRead(Command command) throws AuthenticationException {
		try {
			this.output.writeObject(command);
			this.output.flush();
			Command retVal = (Command) this.input.readObject();
			if (retVal instanceof AuthenticationCmd) {
				AuthenticationCmd cmd = (AuthenticationCmd) retVal;
				throw new AuthenticationException(cmd.getMessage());
			}
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

	private String compute(String inStr) {
		MessageDigest md5 = null;
		byte[] byteArray = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
			byteArray = inStr.getBytes("UTF-8");
		} catch (NoSuchAlgorithmException e) {
			Helper.getInstance().systemCrashHandler(e);
			return "";
		} catch (UnsupportedEncodingException e) {
			Helper.getInstance().systemCrashHandler(e);
			return "";
		}

		byte[] md5Bytes = md5.digest(byteArray);
		StringBuffer hexValue = new StringBuffer();
		for (int i = 0; i < md5Bytes.length; i++) {
			int val = ((int) md5Bytes[i]) & 0xff;
			if (val < 16)
				hexValue.append("0");
			hexValue.append(Integer.toHexString(val));
		}
		return hexValue.toString();
	}

	public synchronized boolean login(String username, String passwd) {
		if (!this.isConnected())
			return false;
		LoginCmd cmd = new LoginCmd();
		cmd.setUserName(username);
		cmd.setPassWord(this.compute(passwd));
		try {
			Command retVal = this.sendAndRead(cmd);
			log.debug("Server said: " + retVal);
			this.loggedIn = retVal instanceof LoginCmd && retVal.commandSucceded();
			return this.loggedIn;
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	public synchronized boolean verCheck() {
		if (!this.isConnected())
			return false;
		HelloCmd cmd = new HelloCmd();
		cmd.setProtocolVersionString(TrmEngine.VERSION);
		try {
			Command retVal = this.sendAndRead(cmd);
			log.debug("Server said: " + retVal);
			return retVal instanceof HelloCmd && retVal.commandSucceded();
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	public synchronized Gallery newGallery(String location, String date) {
		NewGalleryCmd cmd = new NewGalleryCmd();
		Gallery requestGallery = new Gallery();
		requestGallery.setDate(date);
		requestGallery.setLocation(location);
		cmd.setGallery(requestGallery);
		try {
			Command retVal = this.sendAndRead(cmd);
			log.debug("Server said: " + retVal);
			if (retVal instanceof NewGalleryCmd) {
				NewGalleryCmd response = (NewGalleryCmd) retVal;
				return response.getGallery();
			}
			return null;
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return null;
		}
	}

	public synchronized List<Location> getLocations() {
		GetLocationsCmd cmd = new GetLocationsCmd();
		try {
			Command retVal = this.sendAndRead(cmd);
			log.debug("Server said: " + retVal);
			if (retVal instanceof GetLocationsCmd) {
				GetLocationsCmd response = (GetLocationsCmd) retVal;
				return response.getLocations();
			}
			return null;
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return null;
		}
	}

	protected synchronized ArrayList<Gallery> getGalleriesFor(String date) {
		GetGalleriesCmd cmd = new GetGalleriesCmd();
		cmd.setDate(date);
		try {
			Command retVal = this.sendAndRead(cmd);
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
		while ((offset < bytes.length) && ((numRead = is.read(bytes, offset, bytes.length - offset)) >= 0)) {
			offset += numRead;
		}
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}
		is.close();
		return bytes;
	}

	public synchronized void stopIT() {
		this.Running = false;
	}

	protected boolean unLockLocation(Gallery gal) {
		try {
			UnLockPathCmd cmd = new UnLockPathCmd();
			cmd.setDate(gal.getDate());
			cmd.setLocation(gal.getLocation());
			cmd.setSuffix(gal.getSuffix());
			Command retVal = this.sendAndRead(cmd);
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
			Command retVal = this.sendAndRead(cmd);
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
				MainWindow.getInstance().addStatusLine("Ungültige Location.");
				return false;
			} else if (resp.getErrorCode() == LockPathCmd.ERROR_LOC_NOTFREE) {
				MainWindow.getInstance().addStatusLine("Location in Benutzung.");
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

					retVal = this.sendAndRead(cmd);
					log.debug("Server said: " + retVal);
					if (retVal instanceof SaveFileCmd && retVal.commandSucceded()) {
						log.info("Datei " + this.akt.getAbsolutePath() + " gesendet");
						this.akt.delete();
					} else {
						log.info("Datei " + this.akt.getName() + " nicht gesendet");
						MainWindow.getInstance().addStatusLine("Konnte " + this.akt.getName() + " nicht senden");
					}
				}
			}
			SaveGalleryCmd cmd = new SaveGalleryCmd();
			cmd.setGallery(this.gallery);

			retVal = this.sendAndRead(cmd);
			log.debug("Server said: " + retVal);
			if (retVal instanceof SaveGalleryCmd && retVal.commandSucceded()) {
				MainWindow.getInstance().addStatusLine("Galerie gespeichert");
			} else {
				MainWindow.getInstance().addStatusLine("Fehler bei Datenbankeintrag");
			}
			MainWindow.getInstance().addStatusLine("Beende Übertragung");
			this.disconnect();
			MainWindow.getInstance().addStatusLine("Verbindung beendet");
			sleep(10);
		} catch (AuthenticationException authEx) {
			MainWindow.getInstance().addStatusLine(authEx.getMessage());
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			this.Running = false;
		}
		MainWindow.getInstance().setUploadProgress(1000);
	}

	public synchronized void disconnect() {
		try {
			this.keepAliveTimer.cancel();
			this.Running = false;
			this.sendAndRead(new QuitCmd());
			this.output.close();
			this.input.close();
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
		}
	}

	protected Gallery getGallery() {
		return gallery;
	}

	protected void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	@Override
	protected void finalize() throws Throwable {
		this.keepAliveTimer.cancel();
	}

	private class KeepAliveTimerTask extends TimerTask {
		private final Logger log = Logger.getLogger(KeepAliveTimerTask.class);

		@Override
		public void run() {
			if (Transmitter.this.isConnected()) {
				log.debug("sending PingCmd");
				try {
					log.debug("Server said: " + Transmitter.this.sendAndRead(new PingCmd()));
				} catch (AuthenticationException authEx) {
				}
			}
		}
	}
}
