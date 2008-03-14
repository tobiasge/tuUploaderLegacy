package com.teamulm.uploadsystem.protocol;

import com.teamulm.uploadsystem.data.Gallery;

public class NewGalleryCmd extends Command {

	private static final long serialVersionUID = 750758930754656727L;

	private String location;

	private String date;

	private Gallery gallery;

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public NewGalleryCmd() {
		super();
	}

	public NewGalleryCmd(boolean serverResponse) {
		super(serverResponse);
	}	public String toString() {
		String toString = "NewGalleryCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request): for path " + this.getDate());
		}
		return toString;
	}
}
