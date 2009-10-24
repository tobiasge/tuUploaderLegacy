package com.teamulm.uploadsystem.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class Gallery implements Serializable, Comparable<Gallery> {

	public final static DateTimeFormatter GALLERY_DATE_FORMAT = DateTimeFormat.forPattern("dd-MM-yyyy"); //$NON-NLS-1$

	private static final long serialVersionUID = -9028890879729734915L;

	public static String getPath(String location, LocalDate date, int suffix) {
		if (suffix == 0) {
			return location + System.getProperty("file.separator") + Gallery.GALLERY_DATE_FORMAT.print(date) //$NON-NLS-1$
				+ System.getProperty("file.separator"); //$NON-NLS-1$
		} else {
			return location + System.getProperty("file.separator") + Gallery.GALLERY_DATE_FORMAT.print(date) + "-" //$NON-NLS-1$ //$NON-NLS-2$
				+ suffix + System.getProperty("file.separator"); //$NON-NLS-1$
		}
	}

	private LocalDate date;

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

	public int compareTo(Gallery rhs) {
		Gallery lhs = this;
		return new CompareToBuilder().append(lhs.date, rhs.date).append(lhs.location, rhs.location).append(lhs.title,
			rhs.title).toComparison();
	}

	public void deletedPicture(int picture) {
		if (null == this.deletedPictures) {
			this.deletedPictures = new ArrayList<Integer>();
		}
		this.deletedPictures.add(Integer.valueOf(picture));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Gallery) {
			Gallery rhs = (Gallery) obj;
			Gallery lhs = (Gallery) this;
			return new EqualsBuilder().append(lhs.galid, rhs.galid).append(lhs.location, rhs.location).append(lhs.date,
				rhs.date).append(lhs.suffix, rhs.suffix).isEquals();
		}
		return false;
	}

	public LocalDate getDate() {
		return this.date;
	}

	public List<Integer> getDeletedPictures() {
		return this.deletedPictures;
	}

	public String getDesc() {
		return this.desc;
	}

	public int getGalid() {
		return this.galid;
	}

	public String getLocation() {
		return this.location;
	}

	public String getPath() {
		return Gallery.getPath(this.location, this.date, this.suffix);
	}

	public List<User> getPhotographers() {
		return this.photographers;
	}

	public int getPictures() {
		return this.pictures;
	}

	public int getSuffix() {
		return this.suffix;
	}

	public String getTitle() {
		return this.title;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(11, 15).append(this.galid).append(this.location).append(this.date).append(
			this.suffix).toHashCode();
	}

	public boolean isIntern() {
		return this.intern;
	}

	public boolean isNewGallery() {
		return this.newGallery;
	}

	public boolean isPictureDeleted(int picure) {
		if (null == this.deletedPictures) {
			return false;
		}
		for (Integer integer : this.deletedPictures) {
			if (null != integer && integer.intValue() == picure) {
				return true;
			}
		}
		return false;
	}

	public void restorePicture(int picture) {
		if (null == this.deletedPictures) {
			return;
		}
		this.deletedPictures.remove(Integer.valueOf(picture));
	}

	public void setDate(LocalDate date) {
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

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("Galid", this.galid).append(
			"Location", this.location).append("Date", this.date).append("Suffix", this.suffix).toString();
	}
}
