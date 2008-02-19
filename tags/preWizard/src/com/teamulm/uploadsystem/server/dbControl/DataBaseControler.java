package com.teamulm.uploadsystem.server.dbControl;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class DataBaseControler {

	private static final Logger log = Logger.getLogger(DataBaseControler.class);

	private static final String CONFIG = "allServers.xml";

	private static DataBaseControler instance;

	private HashMap<String, DBConn> allDataBases;

	private HashMap<String, DBConn> tableDataBaseMap;

	private DataBaseControler() {
		this.tableDataBaseMap = new HashMap<String, DBConn>();
		this.allDataBases = new HashMap<String, DBConn>();
		this.initConnections(DataBaseControler.CONFIG);
	}

	public static DataBaseControler getInstance() {
		if (null == DataBaseControler.instance) {
			DataBaseControler.instance = new DataBaseControler();
		}
		return DataBaseControler.instance;
	}

	public DBConn getDataBaseForTable(String table) {
		DBConn dBase = null;
		if (this.tableDataBaseMap.containsKey(table)
				|| this.searchDataBaseFor(table)) {
			dBase = this.tableDataBaseMap.get(table);
			return dBase;
		} else {
			return dBase;
		}
	}

	private boolean searchDataBaseFor(String table) {
		try {
			Iterator<DBConn> dbIt = this.allDataBases.values().iterator();
			while (dbIt.hasNext()) {
				DBConn searchHere = dbIt.next();
				PreparedStatement searchQuery = searchHere.getConnection()
						.prepareStatement(
								"SELECT COUNT(*) FROM information_schema.tables WHERE TABLE_NAME = "
										+ "? AND TABLE_SCHEMA = ?");
				searchQuery.setString(1, table);
				searchQuery.setString(2, searchHere.getConnection()
						.getCatalog());
				ResultSet searchRes = searchQuery.executeQuery();
				searchRes.next();
				if (1 == searchRes.getInt(1)) {
					this.tableDataBaseMap.put(table, searchHere);
					return true;
				}
			}
			return false;
		} catch (SQLException sqlEx) {
			return false;
		}
	}

	private void initConnections(String configFileName) {
		File configFile = new File(configFileName);
		if (!configFile.canRead()) {
			return;
		}
		try {
			SAXBuilder builder = new SAXBuilder();
			Document config = builder.build(configFile);
			Element servers = config.getRootElement();
			String dbDriv = servers.getChildText("driver");
			List<?> serverList = servers.getChildren("server");
			Iterator<?> elementIt = serverList.iterator();
			while (elementIt.hasNext()) {
				Object next = elementIt.next();
				if (!(next instanceof Element))
					continue;
				Element serv = (Element) next;
				String serverName = serv.getChildText("servername");
				String dbHost = serv.getChildText("host");
				String dbUser = serv.getChildText("user");
				String dbPass = serv.getChildText("pass");
				String dbBase = serv.getChildText("base");
				String dbURL = "jdbc:mysql://"
						+ dbHost
						+ "/"
						+ dbBase
						+ "?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true";
				this.allDataBases.put(serverName, new DBConn(dbURL, dbUser,
						dbPass, dbDriv, serverName));
				log.info("Connection for " + serverName + " started");
			}
		} catch (IOException IOEx) {
		} catch (JDOMException JDOMEEx) {
		}
	}
}
