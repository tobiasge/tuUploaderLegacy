package com.teamulm.uploadsystem.data;

import java.io.File;
import java.io.Serializable;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.server.DBConn;

public class Gallery implements Serializable {

	private static final long serialVersionUID = -9028890879729734915L;

	private static final Logger log = Logger.getLogger(Gallery.class);

	private String location;

	private String desc;

	private String title;

	private String date;

	private int galid;

	private int pictures;

	private int suffix;

	private boolean intern;

	private boolean newGallery;

	public Gallery() {
		this.galid = -1;
		this.intern = false;
		this.pictures = 0;
		this.suffix = 0;
		this.newGallery = true;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public int getGalid() {
		return galid;
	}

	public void setGalid(int galid) {
		this.galid = galid;
	}

	public boolean isIntern() {
		return intern;
	}

	public void setIntern(boolean intern) {
		this.intern = intern;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public int getPictures() {
		return pictures;
	}

	public void setPictures(int pictures) {
		this.pictures = pictures;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPath() {
		if (this.suffix == 0) {
			return this.location + System.getProperty("file.separator")
					+ this.date + System.getProperty("file.separator");
		} else {
			return this.location + System.getProperty("file.separator")
					+ this.date + "-" + this.suffix
					+ System.getProperty("file.separator");
		}
	}

	public int getSuffix() {
		return suffix;
	}

	public void setSuffix(int suffix) {
		this.suffix = suffix;
	}

	public boolean isNewGallery() {
		return newGallery;
	}

	public void setNewGallery(boolean newGallery) {
		this.newGallery = newGallery;
	}

	public static Gallery getGallery(String baseDir, String location,
			String date) {
		Gallery retVal;
		File dir = new File(baseDir + location
				+ System.getProperty("file.separator") + date);
		if (!dir.exists()) {
			dir.mkdirs();
			log.info("New Location dir created " + dir.getAbsolutePath());
		}
		retVal = DBConn.getInstance().getGallery(location, date);
		if (null == retVal) {
			retVal = new Gallery();
			retVal.setDate(date);
			retVal.setLocation(location);
		}
		return retVal;
	}
}
