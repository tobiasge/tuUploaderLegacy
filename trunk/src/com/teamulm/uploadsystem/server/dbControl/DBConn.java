package com.teamulm.uploadsystem.server.dbControl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

public class DBConn {

	private static final Logger log = Logger.getLogger(com.teamulm.uploadsystem.server.dbControl.DBConn.class);

	/** ******************************************* */
	/** ***************ServerConfig**************** */
	private String dbDriv;

	private String dbPass;

	private String dbURL;

	private String dbUser;
	private Timer keepAliveTimer;
	/** ******************************************* */

	private Connection mySQL;

	private String serverName;

	protected DBConn(String dbURL, String dbUser, String dbPass, String dbDriv, String serverName) {
		this.serverName = serverName;
		this.dbDriv = dbDriv;
		this.dbPass = dbPass;
		this.dbURL = dbURL;
		this.dbUser = dbUser;
		this.connect();
		keepAliveTimer = new Timer("KeepAlive Timer for " + this.serverName, true);
		keepAliveTimer.scheduleAtFixedRate(new KeepAliveTimerTask(), 0, 1000 * 55);
	}

	public ResultSet executeQuery(String query) {
		try {
			Statement stmt = this.mySQL.createStatement();
			return stmt.executeQuery(query);
		} catch (SQLException sqlException) {
			log.error("Could not execute query for " + this.serverName, sqlException);
			return null;
		}
	}

	public int executeUpdate(String query) {
		try {
			Statement stmt = this.mySQL.createStatement();
			return stmt.executeUpdate(query);
		} catch (SQLException sqlException) {
			log.error("Could not execute update for " + this.serverName, sqlException);
			return -1;
		}
	}

	public PreparedStatement prepareStatement(String query) {
		try {
			return this.getConnection().prepareStatement(query);
		} catch (SQLException sqlException) {
			log.error("Could not prepare statement for " + this.serverName, sqlException);
			return null;
		}
	}

	private boolean connect() {
		try {
			if (null != this.mySQL) {
				this.mySQL.close();
			}
			Class.forName(this.dbDriv);
			this.mySQL = DriverManager.getConnection(this.dbURL, this.dbUser, this.dbPass);
			this.mySQL.setAutoCommit(true);
			return true;
		} catch (SQLException sqlException) {
			log.error("Could not connect to database", sqlException);
		} catch (ClassNotFoundException classNotFoundException) {
			log.error("Could not load database driver", classNotFoundException);
		}
		return false;
	}

	private boolean isConnected() {
		try {
			if (null == this.mySQL || this.mySQL.isClosed() || this.mySQL.isReadOnly()) {
				log.warn("Connection to " + this.serverName + " lost");
				return false;
			} else {
				this.mySQL.createStatement().executeQuery("SELECT NOW()");
				return true;
			}
		} catch (SQLException sqlException) {
			log.warn("Connection to " + this.serverName + " lost", sqlException);
			return false;
		}
	}

	protected Connection getConnection() {
		return this.mySQL;
	}

	class KeepAliveTimerTask extends TimerTask {

		int counter;

		public KeepAliveTimerTask() {
			this.counter = 0;
		}

		@Override
		public void run() {
			try {
				if (!DBConn.this.isConnected()) {
					DBConn.this.connect();
				}
				DBConn.this.mySQL.createStatement().executeQuery("SELECT NOW()");
				this.counter++;
				if (0 == (this.counter % 100)) {
					this.counter = 0;
					log.info("Success in KeepAliveTimerTask for " + DBConn.this.serverName);
				}
			} catch (SQLException sqlException) {
				log.warn("Failure in KeepAliveTimerTask for " + DBConn.this.serverName, sqlException);
				DBConn.this.connect();
			}
		}
	}
}
