package com.teamulm.uploadsystem.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;

public class Gallery implements Serializable, Comparable<Gallery> {

	private static final long serialVersionUID = -9028890879729734915L;

	public static String getPath(String location, String date, int suffix) {
		if (suffix == 0) {
			return location + System.getProperty("file.separator") + date + System.getProperty("file.separator");
		} else {
			return location + System.getProperty("file.separator") + date + "-" + suffix
				+ System.getProperty("file.separator");
		}
	}

	private String date;

	private List<Integer> deletedPictures = null;

	private String desc;

	private int galid;

	private boolean intern;

	private String location;

	private boolean newGallery;

	private List<User> photographers = null;

	private int pictures;

	private int suffix;

	private String title;

	public Gallery() {
		this.galid = -1;
		this.intern = false;
		this.pictures = 0;
		this.suffix = 0;
		this.newGallery = true;
		this.deletedPictures = new ArrayList<Integer>();
		this.photographers = new ArrayList<User>();
	}

	public void deletedPicture(int picture) {
		if (null == this.deletedPictures) {
			this.deletedPictures = new ArrayList<Integer>();
		}
		this.deletedPictures.add(Integer.valueOf(picture));
	}

	public String getDate() {
		return date;
	}

	public List<Integer> getDeletedPictures() {
		return deletedPictures;
	}

	public String getDesc() {
		return desc;
	}

	public int getGalid() {
		return galid;
	}

	public String getLocation() {
		return location;
	}

	public boolean isPictureDeleted(int picure) {
		for (Integer integer : this.deletedPictures) {
			if (integer.intValue() == picure) {
				return true;
			}
		}
		return false;
	}

	public String getPath() {
		if (this.suffix == 0) {
			return this.location + System.getProperty("file.separator") + this.date
				+ System.getProperty("file.separator");
		} else {
			return this.location + System.getProperty("file.separator") + this.date + "-" + this.suffix
				+ System.getProperty("file.separator");
		}
	}

	public List<User> getPhotographers() {
		return photographers;
	}

	public int getPictures() {
		return pictures;
	}

	public int getSuffix() {
		return suffix;
	}

	public String getTitle() {
		return title;
	}

	public boolean isIntern() {
		return intern;
	}

	public boolean isNewGallery() {
		return this.newGallery;
	}

	public void restorePicture(int picture) {
		if (null == this.deletedPictures) {
			return;
		}
		this.deletedPictures.remove(Integer.valueOf(picture));
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDeletedPictures(List<Integer> deletedPictures) {
		this.deletedPictures = deletedPictures;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setGalid(int galid) {
		this.galid = galid;
	}

	public void setIntern(boolean intern) {
		this.intern = intern;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setNewGallery(boolean newGallery) {
		this.newGallery = newGallery;
	}

	public void setPhotographers(List<User> photographers) {
		this.photographers = photographers;
	}

	public void setPictures(int pictures) {
		this.pictures = pictures;
	}

	public void setSuffix(int suffix) {
		this.suffix = suffix;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		return this.location;
	}

	@Override
	public int compareTo(Gallery rhs) {
		Gallery lhs = this;
		return new CompareToBuilder().append(lhs.date, rhs.date).append(lhs.location, rhs.location).append(lhs.title,
			rhs.title).toComparison();
	}
}
