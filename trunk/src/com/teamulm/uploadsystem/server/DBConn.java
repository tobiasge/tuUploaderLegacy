package com.teamulm.uploadsystem.server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.Location;
import com.teamulm.uploadsystem.data.User;
import com.teamulm.uploadsystem.server.dbControl.DataBaseControler;

public class DBConn {

	private static final Logger log = Logger.getLogger(DBConn.class);

	private static final String GALLERY_FIELDS_TO_SELECT = "galid, pictures, suffix, location, description, title, intern, censored, userids, date_gal";

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
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_users_login"))) {
			return retVal;
		}
		try {
			String query = "SELECT userid, passwort_enc, username FROM tu_users_login WHERE username = ? AND teammember = 1";
			request = connection.prepareStatement(query);
			request.setString(1, username);
			result = request.executeQuery();
			result.first();
			retVal = new User(result.getInt("userid"), result.getString("passwort_enc"), result.getString("username"));
		} catch (Exception e) {
			log.error("Failure in getUserForName(): ", e);
		}
		return retVal;
	}

	private User getUserForId(int userId) {
		PreparedStatement request;
		ResultSet result;
		User retVal = null;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_users_login"))) {
			return retVal;
		}
		try {
			String query = "SELECT userid, passwort_enc, username FROM tu_users_login WHERE userid = ?";
			request = connection.prepareStatement(query);
			request.setInt(1, userId);
			result = request.executeQuery();
			result.first();
			retVal = new User(result.getInt("userid"), result.getString("passwort_enc"), result.getString("username"));
		} catch (Exception e) {
			log.error("Failure in getUserForId(): ", e);
		}
		return retVal;
	}

	public Gallery getGallery(String location, String date, int suffix) {
		PreparedStatement request;
		ResultSet result;
		Gallery retVal = null;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos"))) {
			return retVal;
		}
		String query = "SELECT " + DBConn.GALLERY_FIELDS_TO_SELECT + " FROM tu_fotos WHERE location = ? "
			+ "AND date_gal = STR_TO_DATE(?, '%d-%m-%Y') AND suffix = ?";
		try {
			request = connection.prepareStatement(query);
			request.setString(1, location);
			request.setString(2, date);
			request.setInt(3, suffix);
			result = request.executeQuery();
			if (!result.first()) {
				return retVal;
			}
			retVal = this.getFromRow(result);
		} catch (Exception e) {
			log.error("Failure in getGallery(): ", e);
		}
		return retVal;
	}

	public int getNextSuffixFor(String location, String date) {
		PreparedStatement request;
		ResultSet result;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos"))) {
			return 0;
		}
		String query = "SELECT IF(MAX(suffix) IS NULL, -1, MAX(suffix)) AS suffix FROM tu_fotos WHERE location = ? "
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
			log.error("Failure in getNextSuffixFor(): ", e);
		}
		return 0;
	}

	public boolean getGalleries(String date, ArrayList<Gallery> galleries) {
		PreparedStatement request;
		ResultSet result;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos"))) {
			return false;
		}
		String query = "SELECT " + DBConn.GALLERY_FIELDS_TO_SELECT + " FROM tu_fotos WHERE "
			+ "date_gal = STR_TO_DATE(?, '%d-%m-%Y')";
		try {
			request = connection.prepareStatement(query);
			request.setString(1, date);
			result = request.executeQuery();
			while (result.next()) {
				galleries.add(this.getFromRow(result));
			}
		} catch (Exception e) {
			log.error("Failure in getGalleries(): ", e);
			return false;
		}
		return true;
	}

	public boolean checkLocation(String location) {
		PreparedStatement request;
		ResultSet result;
		boolean retVal = false;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos_locations"))) {
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
			log.error("Failure in checkLocation(): ", e);
		}
		return retVal;
	}

	public boolean saveLastUploadLogEntry(long userid, long uploadedPictures, String galDate, String galLoc) {
		PreparedStatement request;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos_lastUpload"))) {
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
			log.error("Failure in saveLastUploadLogEntry(): ", e);
			return false;
		}
		return true;
	}

	public boolean saveGalleryToDataBase(Gallery gallery, User user) {
		PreparedStatement request;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos"))) {
			return false;
		}
		try {
			if (-1 == gallery.getGalid()) { // Save new gallery
				String query = "INSERT INTO tu_fotos(location,description,suffix,"
					+ "title,userids,pictures,date_gal,intern)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
				request = connection.prepareStatement(query);
				request.setString(1, gallery.getLocation());
				request.setString(2, gallery.getDesc());
				request.setInt(3, gallery.getSuffix());
				request.setString(4, gallery.getTitle());
				request.setInt(5, user.getUserid());
				request.setInt(6, gallery.getPictures());
				request.setDate(7, new java.sql.Date(gallery.getDate().toDateMidnight().toDate().getTime()));
				request.setBoolean(8, gallery.isIntern());
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
			log.error("Failure in saveGalleryToDataBase(): ", e);
			return false;
		}
		return true;
	}

	public List<Location> getLocations() {
		PreparedStatement request;
		ResultSet result;
		com.teamulm.uploadsystem.server.dbControl.DBConn connection = null;
		if (null == (connection = DataBaseControler.getInstance().getDataBaseForTable("tu_fotos_locations"))) {
			return null;
		}
		List<Location> locations = new ArrayList<Location>();
		try {
			String query = "SELECT locID, locName FROM tu_fotos_locations";
			request = connection.prepareStatement(query);
			result = request.executeQuery();
			while (result.next()) {
				Location tmpLocation = new Location(result.getInt("locID"), result.getString("locName"));
				locations.add(tmpLocation);
			}
		} catch (Exception e) {
			log.error("Failure in getLocations(): ", e);
			return null;
		}
		return locations;
	}

	private Gallery getFromRow(ResultSet result) throws SQLException {
		Gallery gallery = new Gallery();
		gallery.setDate(LocalDate.fromDateFields(result.getDate("date_gal")));
		gallery.setLocation(result.getString("location"));
		gallery.setPictures(result.getInt("pictures"));
		gallery.setGalid(result.getInt("galid"));
		gallery.setSuffix(result.getInt("suffix"));
		gallery.setTitle(result.getString("title"));
		gallery.setDesc(result.getString("description"));
		gallery.setIntern(result.getBoolean("intern"));
		String[] userids = StringUtils.split(result.getString("userids"), '&');
		for (String userid : userids) {
			gallery.getPhotographers().add(this.getUserForId(Integer.valueOf(userid).intValue()));
		}
		String[] censoredPictures = StringUtils.split(result.getString("censored"), ',');
		for (String censoredPicture : censoredPictures) {
			gallery.getDeletedPictures().add(Integer.valueOf(censoredPicture));
		}
		gallery.setNewGallery(false);
		return gallery;
	}
}
