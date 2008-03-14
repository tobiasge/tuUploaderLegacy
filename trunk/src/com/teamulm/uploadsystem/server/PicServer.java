package com.teamulm.uploadsystem.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 * @author Tobias Genannt
 */

public class PicServer extends Thread {

	private static final Logger log = Logger.getLogger(PicServer.class);

	private static final int PORT = 1807;

	private static final String SERVERCONFFILE = "server.conf";

	private static PicServer instance;

	private Hashtable<Integer, Thread> childMap;

	private ServerSocket listen;

	private Hashtable<String, String> locationsMap;

	private boolean Running;

	public synchronized void logLastUpload(long userid, long uploadedPictures,
			String galDate, String galLoc) {
		DBConn.getInstance().saveLastUploadLogEntry(userid, uploadedPictures,
				galDate, galLoc);
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

	public void unlockLocation(String loc) {
		String user = this.locationsMap.remove(loc);
		log.info("Location unlocked: " + loc + " by " + user);

	}

	public void signoff(int threadHash) {
		this.childMap.remove(new Integer(threadHash));
	}

	public boolean hasClients() {
		return !this.childMap.isEmpty();
	}

	public synchronized void requestStop() {
		this.Running = false;
	}

	public synchronized boolean isRunning() {
		return this.Running;
	}

	@Override
	public void run() {
		try {
			this.listen = new ServerSocket(PicServer.PORT);
			if (this.listen == null) {
				log.error("Could not create communication socket");
				System.exit(1);
			}
			log.info("New ServerSocket created - listening on Port: " + PORT);
			while (this.isRunning()) {
				Socket socket = this.listen.accept();
				log.info("Connection from: "
						+ socket.getInetAddress().getHostAddress());
				UploadServ comlink = new UploadServ(this, socket);
				this.childMap.put(new Integer(comlink.hashCode()), comlink);
				comlink.start();
			}
		} catch (IOException e) {
			log.error("Could not create communication socket: "
					+ e.getMessage());

		}
	}

	private PicServer() {
		this.Running = true;
		log.info("Server startup");
		log.info("--------------------");
		this.childMap = new Hashtable<Integer, Thread>();
		this.locationsMap = new Hashtable<String, String>();
	}

	public static PicServer getInstance() {
		return PicServer.instance;
	}

	public Properties getServerConf() {
		Properties serverConf = new Properties();
		try {
			serverConf
					.loadFromXML(new FileInputStream(PicServer.SERVERCONFFILE));
		} catch (IOException e) {
		}
		return serverConf;
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
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				PicServer.instance.requestStop();
				try {
					log.info("INFO: Server closing");
					int i = 0;
					while (PicServer.instance.hasClients()) {
						sleep(10000);
						log.info("Waiting for Clients to disconnect. Round "
								+ i);
						i++;
					}
					log.info("Finished");
					log.info("--------------------");
				} catch (Exception e) {
				}
			}
		});
	}
}
