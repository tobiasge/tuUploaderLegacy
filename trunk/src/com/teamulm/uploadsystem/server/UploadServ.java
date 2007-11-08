package com.teamulm.uploadsystem.server;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.security.MessageDigest;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.User;
import com.teamulm.uploadsystem.protocol.Command;
import com.teamulm.uploadsystem.protocol.HelloCmd;
import com.teamulm.uploadsystem.protocol.LockPathCmd;
import com.teamulm.uploadsystem.protocol.LoginCmd;
import com.teamulm.uploadsystem.protocol.QuitCmd;
import com.teamulm.uploadsystem.protocol.SaveFileCmd;
import com.teamulm.uploadsystem.protocol.SaveGalleryCmd;

/**
 * @author Tobias Genannt
 */
public class UploadServ extends Thread {

	private static final Logger log = Logger.getLogger(UploadServ.class);

	private static final String VER = "4.0";

	private Socket client;

	private int uploaded;

	private User user;

	private Gallery gallery;

	private String clientip;

	private ObjectInputStream input;

	private ObjectOutputStream output;

	private boolean active;

	private boolean accepted;

	private boolean hasLock;

	private String baseDir;

	private void initServer() {
		Properties serverConf = PicServer.getInstance().getServerConf();
		this.baseDir = serverConf.getProperty("baseDir");
	}

	public UploadServ(PicServer master, Socket so) {
		super();
		this.hasLock = false;
		this.uploaded = 0;
		this.client = so;
		this.clientip = so.getInetAddress().getHostAddress();
		this.accepted = false;
		this.initServer();
		try {
			this.client.setSoTimeout(1000 * 60 * 3);
			this.input = new ObjectInputStream(so.getInputStream());
			this.output = new ObjectOutputStream(so.getOutputStream());
			this.active = true;
		} catch (Exception e) {
			log.error(this.clientip + ": could not create com streams");
		}
	}

	private String compute(String inStr) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
		}
		char[] charArray = inStr.toCharArray();
		byte[] byteArray = new byte[charArray.length];
		for (int i = 0; i < charArray.length; i++)
			byteArray[i] = (byte) charArray[i];
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

	private boolean authUser(String user, String passwd) {
		// encrypt password
		String encPass = this.compute(passwd);
		this.user = DBConn.getInstance().getUserForName(user);
		return (null != passwd) && (null != this.user)
				&& (encPass.equalsIgnoreCase(this.user.getPassword()));
	}

	private void ExceptionHandler(Exception exc) {
		if (exc instanceof SocketTimeoutException) {
			log.error(this.clientip + ": Client timed out");
			this.cleanUp();
		} else if (exc instanceof IOException) {
			log.error(this.clientip + ": I/O Failure");
			this.cleanUp();
		} else if (exc instanceof SocketException) {
			log.error(this.clientip + ": Lost Connection to " + this.clientip);
			this.cleanUp();
		} else {
			log.error(this.clientip + ": Unkn6wn Exception: " + exc.getClass());
			exc.printStackTrace(System.err);
			this.cleanUp();
		}
	}

	private boolean makeDBEntry() {
		if (DBConn.getInstance().saveGalleryToDataBase(this.gallery, this.user)) {
			log.info(this.clientip + ": Gallery is saved");
			return true;
		} else {
			log.error(this.clientip + ": Gallery saving failed");
			return false;
		}
	}

	private boolean saveFile(SaveFileCmd cmd) {
		try {
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(this.baseDir
							+ this.gallery.getPath() + cmd.getFileName())));
			out.write(cmd.getFileContent(), 0, cmd.getFileSize());
			out.flush();
			out.close();
			this.uploaded++;
			log.info(this.clientip + ": File " + cmd.getFileName()
					+ " with size " + cmd.getFileSize() + " saved");
			return true;
		} catch (Exception e) {
			log.error(this.clientip + ": File " + cmd.getFileName()
					+ " with size " + cmd.getFileSize() + " not saved");
			this.ExceptionHandler(e);
			return false;
		}
	}

	private void cleanUp() {
		this.active = false;
		if (this.hasLock) {
			PicServer.getInstance().unlockLocation(this.gallery.getPath());
		}
		PicServer.getInstance().signoff(this.hashCode());
		log.info(this.clientip + ": connection to client terminated");
	}

	private Command readCommand() {
		try {
			Command retVal = (Command) this.input.readObject();
			return retVal;
		} catch (ClassCastException e) {
			log.error(this.clientip
					+ ": readCommand(): failed with class problem");
			this.ExceptionHandler(e);
		} catch (ClassNotFoundException e) {
			log.error(this.clientip
					+ ": readCommand(): failed with class loader problem");
			this.ExceptionHandler(e);
		} catch (IOException e) {
			log
					.error(this.clientip
							+ ": readCommand(): failed with IO problem");
			this.ExceptionHandler(e);
		}
		return null;
	}

	@Override
	public void run() {
		try {
			Command cmd = this.readCommand();

			// Version des Clients ueberpruefen
			if (cmd instanceof HelloCmd) {
				HelloCmd request = (HelloCmd) cmd;
				HelloCmd response = new HelloCmd(true);
				if (!request.getProtocolVersionString().equalsIgnoreCase(
						UploadServ.VER)) {
					log.error(this.clientip
							+ ": Tried to connect with bad version: "
							+ request.getProtocolVersionString());
					response.setErrorMsg("Protocolversion missmatch");
					response.setSuccess(false);
					this.output.writeObject(response);
					this.output.flush();
					this.cleanUp();
				} else {
					response.setProtocolVersionString(UploadServ.VER);
					response.setSuccess(true);
					this.output.writeObject(response);
					this.output.flush();
				}
			} else {
				this.active = false;
				log.error(this.clientip
						+ ": Client did not send HELLO with version");
			}
			// User und Passwd testen
			if (this.active) {
				cmd = this.readCommand();
				if (cmd instanceof LoginCmd) {
					LoginCmd request = (LoginCmd) cmd;
					LoginCmd response = new LoginCmd(true);
					if (!this.authUser(request.getUserName(), request
							.getPassWord())) {
						response.setErrorMsg("Wrong username or password for "
								+ request.getUserName());
						response.setSuccess(false);
						this.output.writeObject(response);
						this.output.flush();
						log.info(this.clientip
								+ ": Bad user or password; User: "
								+ request.getUserName());
						this.cleanUp();
					} else {
						this.accepted = true;
						response.setSuccess(true);
						log.info(this.clientip + ": User "
								+ request.getUserName() + " accepted");
						this.output.writeObject(response);
						this.output.flush();
					}
				} else {
					this.active = false;
					log.error(this.clientip + ": No user send");
					this.cleanUp();
				}
			}
			while (this.active) {
				cmd = this.readCommand();
				if (this.accepted && cmd instanceof SaveFileCmd) {
					SaveFileCmd request = (SaveFileCmd) cmd;
					SaveFileCmd response = new SaveFileCmd(true);
					response.setSuccess(this.saveFile(request));
					this.output.writeObject(response);
					this.output.flush();
				} else if (this.accepted && cmd instanceof SaveGalleryCmd) {
					SaveGalleryCmd request = (SaveGalleryCmd) cmd;
					SaveGalleryCmd response = new SaveGalleryCmd(true);
					log.info(this.clientip + ": DB Entry requested");
					this.gallery.setPictures((this.uploaded / 2 + this.gallery
							.getPictures()));
					if (request.getGallery().isNewGallery()) {
						this.gallery.setDesc(request.getGallery().getDesc());
						this.gallery.setTitle(request.getGallery().getTitle());
						this.gallery.setIntern(request.getGallery().isIntern());
					}
					if (this.makeDBEntry()) {
						if (this.uploaded > 2) {
							PicServer.getInstance().logLastUpload(
									this.user.getUserid(), this.uploaded / 2,
									this.gallery.getDate(),
									this.gallery.getLocation());
						}
						log.info(this.clientip + ": DB Entry done");
						response.setSuccess(true);
						response.setGallery(this.gallery);
						this.output.writeObject(response);
						this.output.flush();
					} else {
						response.setSuccess(false);
						response.setGallery(this.gallery);
						response.setErrorMsg("gallery data could not be saved");
						log.error(this.clientip + ": DB Entry failed");
						this.output.writeObject(response);
						this.output.flush();
					}
				} else if (this.accepted && cmd instanceof LockPathCmd) {
					LockPathCmd request = (LockPathCmd) cmd;
					LockPathCmd response = new LockPathCmd(true);
					if (!DBConn.getInstance().checkLocation(
							request.getLocation())) {
						response.setErrorCode(Command.ERROR_LOC_BADLOC);
						response.setErrorMsg("location not valid");
						response.setSuccess(false);
						this.output.writeObject(response);
						this.output.flush();
						log.info(this.clientip + ": invalid location selected");
						this.cleanUp();
					} else if (PicServer.getInstance().lockLocation(
							request.getPath(), this.user.getUsername())) {
						response.setSuccess(true);
						this.hasLock = true;
						this.gallery = Gallery.getGallery(this.baseDir, request
								.getLocation(), request.getDate());
						response.setStartNumber(this.gallery.getPictures() + 1);
						this.output.writeObject(response);
						this.output.flush();
					} else {
						response.setErrorCode(Command.ERROR_LOC_NOTFREE);
						response.setErrorMsg("location is in use");
						response.setSuccess(false);
						this.output.writeObject(response);
						this.output.flush();
						log.info(this.clientip + ": selected location is used");
						this.cleanUp();
					}
				} else if (this.accepted && cmd instanceof QuitCmd) {
					log.info(this.clientip + ": client closed connection");
					this.client.close();
					this.cleanUp();
				} else {
					if (null == cmd) {
						log.error(this.clientip + ": client command was null");
					} else {
						log.error(this.clientip + ": client used bad command: "
								+ cmd.getClass());
					}
					this.active = false;
					this.cleanUp();
				}
			}
		} catch (Exception e) {
			this.ExceptionHandler(e);
		}
	}
}
