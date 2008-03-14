package com.teamulm.uploadsystem.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.User;
import com.teamulm.uploadsystem.server.dbControl.DataBaseControler;

public class DBConn {

	private static final Logger log = Logger.getLogger(DBConn.class);

	private static DBConn instance;

	private DBConn() {
	}

	public static DBConn getInstance() {
		if (null == DBConn.instance) {
			DBConn.instance = new DBConn();
		}
		return DBConn.instance;
	}

	public User getUserForName(String username) {
		PreparedStatement request;
		ResultSet result;
		User retVal = null;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_users_login"))) {
			return retVal;
		}
		try {
			String query = "SELECT userid, passwort_enc, username FROM tu_users_login WHERE username = ? AND teammember = 1";
			request = connection.prepareStatement(query);
			request.setString(1, username);
			result = request.executeQuery();
			result.first();
			retVal = new User(result.getInt("userid"), result
					.getString("passwort_enc"), result.getString("username"));
		} catch (Exception e) {
			log.error("Failure in getUserForName(): " + e.getClass());
			log.error("Failure in getUserForName(): " + e.getMessage());
		}
		return retVal;
	}

	public Gallery getGallery(String location, String date, int suffix) {
		PreparedStatement request;
		ResultSet result;
		Gallery retVal = null;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_fotos"))) {
			return retVal;
		}
		String query = "SELECT galid, pictures, suffix, tt FROM tu_fotos WHERE location = ? "
				+ "AND date_gal = STR_TO_DATE(?, '%d-%m-%Y') AND suffix = ?";
		try {
			request = connection.prepareStatement(query);
			request.setString(1, location);
			request.setString(2, date);
			request.setInt(0, suffix);
			result = request.executeQuery();
			if (!result.first())
				return retVal;
			retVal = new Gallery();
			retVal.setDate(date);
			retVal.setLocation(location);
			retVal.setPictures(result.getInt("pictures"));
			retVal.setGalid(result.getInt("galid"));
			retVal.setSuffix(result.getInt("suffix"));
			retVal.setTitle(result.getString("title"));
			retVal.setDesc(result.getString("description"));
			retVal.setNewGallery(false);
		} catch (Exception e) {
			log.error("Failure in getGallery(): " + e.getClass());
			log.error("Failure in getGallery(): " + e.getMessage());
		}
		return retVal;
	}

	public int getNextSuffixFor(String location, String date) {
		PreparedStatement request;
		ResultSet result;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_fotos"))) {
			return 0;
		}
		String query = "SELECT IF(MAX(suffix) IS NULL, 0, MAX(suffix)) AS suffix FROM tu_fotos WHERE location = ? "
				+ "AND date_gal = STR_TO_DATE(?, '%d-%m-%Y')";
		try {
			request = connection.prepareStatement(query);
			request.setString(1, location);
			request.setString(2, date);
			result = request.executeQuery();
			if (!result.first())
				return 0;
			return result.getInt("suffix") + 1;
		} catch (Exception e) {
			log.error("Failure in getNextSuffixFor(): " + e.getClass());
			log.error("Failure in getNextSuffixFor(): " + e.getMessage());
		}
		return 0;
	}

	public boolean getGalleries(String date, ArrayList<Gallery> galleries) {
		PreparedStatement request;
		ResultSet result;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_fotos"))) {
			return false;
		}
		String query = "SELECT galid, pictures, suffix, location, description, title FROM tu_fotos WHERE "
				+ "date_gal = STR_TO_DATE(?, '%d-%m-%Y')";
		try {
			request = connection.prepareStatement(query);
			request.setString(1, date);
			result = request.executeQuery();
			while (result.next()) {
				Gallery tmpGal = new Gallery();
				tmpGal.setDate(date);
				tmpGal.setLocation(result.getString("location"));
				tmpGal.setPictures(result.getInt("pictures"));
				tmpGal.setGalid(result.getInt("galid"));
				tmpGal.setSuffix(result.getInt("suffix"));
				tmpGal.setTitle(result.getString("title"));
				tmpGal.setDesc(result.getString("description"));
				tmpGal.setNewGallery(false);
				galleries.add(tmpGal);
			}
		} catch (Exception e) {
			log.error("Failure in getGalleries(): " + e.getClass());
			log.error("Failure in getGalleries(): " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean checkLocation(String location) {
		PreparedStatement request;
		ResultSet result;
		boolean retVal = false;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_fotos_locations"))) {
			return retVal;
		}
		String query = "SELECT COUNT(*) AS number FROM tu_fotos_locations WHERE locName = ?";
		try {
			request = connection.prepareStatement(query);
			request.setString(1, location);
			result = request.executeQuery();
			result.first();
			retVal = (result.getInt("number") == 1);
		} catch (Exception e) {
			log.error("Failure in checkLocation(): " + e.getClass());
			log.error("Failure in checkLocation(): " + e.getMessage());
		}
		return retVal;
	}

	public boolean saveLastUploadLogEntry(long userid, long uploadedPictures,
			String galDate, String galLoc) {
		PreparedStatement request;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_fotos_lastUpload"))) {
			return false;
		}
		try {
			String query = "INSERT INTO tu_fotos_lastUpload(userid,galleryDate,galleryLocation,pictures) "
					+ "VALUES(?,STR_TO_DATE(?, '%d-%m-%Y'),?,?)";
			request = connection.prepareStatement(query);
			request.setLong(1, userid);
			request.setString(2, galDate);
			request.setString(3, galLoc);
			request.setLong(4, uploadedPictures);
			request.executeUpdate();
		} catch (Exception e) {
			log.error("Failure in saveLastUploadLogEntry(): " + e.getClass());
			log.error("Failure in saveLastUploadLogEntry(): " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean saveGalleryToDataBase(Gallery gallery, User user) {
		PreparedStatement request;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance()
				.getDataBaseForTable("tu_fotos"))) {
			return false;
		}
		try {
			if (-1 == gallery.getGalid()) { // Save new gallery
				String query = "INSERT INTO tu_fotos(location,description,suffix,"
						+ "title,userids,pictures,date_gal,intern)"
						+ " VALUES (?, ?, ?, ?, ?, ?, STR_TO_DATE(?, '%d-%m-%Y'), ?)";
				request = connection.prepareStatement(query);
				request.setString(1, gallery.getLocation());
				request.setString(2, gallery.getDesc());
				request.setInt(3, gallery.getSuffix());
				request.setString(4, gallery.getTitle());
				request.setInt(5, user.getUserid());
				request.setInt(6, gallery.getPictures());
				request.setString(7, gallery.getDate());
				if (gallery.isIntern()) {
					request.setInt(8, 1);
				} else {
					request.setInt(8, 0);
				}
				request.executeUpdate();
			} else { // Update gallery
				String query = "UPDATE tu_fotos SET pictures = ?, userids = CONCAT(userids, ?) where galid = ?";
				request = connection.prepareStatement(query);
				request.setInt(1, gallery.getPictures());
				request.setString(2, "&" + user.getUserid());
				request.setInt(3, gallery.getGalid());
				request.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Failure in saveGalleryToDataBase(): " + e.getClass());
			log.error("Failure in saveGalleryToDataBase(): " + e.getMessage());
			return false;
		}
		return true;
	}
}
