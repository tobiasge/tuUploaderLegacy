package com.teamulm.uploadsystem.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Tobias Genannt
 */

public class PicServer extends Thread {

	private static PicServer instance;

	private static final Logger log = Logger.getLogger(PicServer.class);

	private static final String SERVERCONFFILE = "server.conf";

	public static PicServer getInstance() {
		return PicServer.instance;
	}

	public static void main(String[] args) {
		try {
			Properties logConf = new Properties();
			logConf.load(new FileInputStream("server.log4j.properties"));
			PropertyConfigurator.configure(logConf);
		} catch (Exception e) {

		}
		PicServer.instance = new PicServer();
		PicServer.instance.start();
		Runtime.getRuntime().addShutdownHook(new Thread("ShutDownHook") {
			@Override
			public void run() {
				PicServer.instance.requestStop();
				try {
					log.info("INFO: Server closing");
					int i = 0;
					while (PicServer.instance.hasClients()) {
						sleep(10000);
						log.info("Waiting for Clients to disconnect. Round " + i);
						i++;
					}
					log.info("Finished");
					log.info("--------------------");
				} catch (Exception e) {
				}
			}
		});
	}

	private Hashtable<Integer, Thread> childMap;

	private ServerSocket listen;

	private Hashtable<String, String> locationsMap;

	private boolean running;

	private Properties serverConf = null;

	private PicServer() {
		super("PicServer");
		this.running = true;
		log.info("Server startup");
		log.info("--------------------");
		this.childMap = new Hashtable<Integer, Thread>();
		this.locationsMap = new Hashtable<String, String>();
	}

	public Properties getServerConf() {
		if (null == this.serverConf) {
			this.serverConf = new Properties();
			try {
				this.serverConf.loadFromXML(new FileInputStream(PicServer.SERVERCONFFILE));
			} catch (IOException ioException) {
				log.error("", ioException);
			}
		}
		return this.serverConf;
	}

	public boolean hasClients() {
		return !this.childMap.isEmpty();
	}

	public synchronized boolean isRunning() {
		return this.running;
	}

	public boolean lockLocation(String loc, String user) {
		if (this.locationsMap.containsKey(loc)) {
			return false;
		} else {
			this.locationsMap.put(loc, user);
			log.info("Location locked: " + loc + " by " + user);
		}
		return true;
	}

	public synchronized void logLastUpload(long userid, long uploadedPictures, String galDate, String galLoc) {
		DBConn.getInstance().saveLastUploadLogEntry(userid, uploadedPictures, galDate, galLoc);
	}

	public synchronized void requestStop() {
		this.running = false;
	}

	@Override
	public void run() {
		try {
			this.listen = new ServerSocket(Integer.parseInt(this.getServerConf().getProperty("serverPort",
				"1807")));
			if (this.listen == null) {
				log.error("Could not create communication socket");
				System.exit(1);
			}
			this.listen.setSoTimeout(1000 * 60);
		} catch (IOException ioException) {
			log.error("Could not create communication socket", ioException);
			System.exit(1);
		}

		log.info("New ServerSocket created - listening on Port: " + this.listen.getLocalPort());
		while (this.isRunning()) {
			try {
				Socket socket = this.listen.accept();
				log.info("Connection from: " + socket.getInetAddress().getHostAddress());
				if (this.isRunning()) {
					UploadServ comlink = new UploadServ(this, socket, this.childMap.size() + 1);
					this.childMap.put(new Integer(comlink.hashCode()), comlink);
					comlink.start();
				} else {
					log.warn("Shudown in progress. No new connections are accepted");
					socket.close();
				}
			} catch (SocketTimeoutException socketTimeoutException) {
				// Ignored, but used to check if we have to shutdown
			} catch (IOException ioException) {
				log.error("Error creating communication link", ioException);
			}
		}
	}

	public void signoff(int threadHash) {
		this.childMap.remove(new Integer(threadHash));
	}

	public void unlockLocation(String loc) {
		String user = this.locationsMap.remove(loc);
		log.info("Location unlocked: " + loc + " by " + user);
	}
}
