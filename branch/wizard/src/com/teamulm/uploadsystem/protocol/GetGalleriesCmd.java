package com.teamulm.uploadsystem.protocol;

import java.util.ArrayList;

import com.teamulm.uploadsystem.data.Gallery;

public class GetGalleriesCmd extends Command {

	private static final long serialVersionUID = 1412252946320401664L;

	private String galDate;

	private ArrayList<Gallery> galleries;

	public GetGalleriesCmd() {
		super();
	}

	public GetGalleriesCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public void setDate(String date) {
		this.galDate = date;
	}

	public String getDate() {
		return this.galDate;
	}

	public void setGalleries(ArrayList<Gallery> galleries) {
		this.galleries = galleries;
	}

	public ArrayList<Gallery> getGalleries() {
		if (this.isServerResponse()) {
			return this.galleries;
		}
		return null;
	}
	
	public String toString() {
		String toString = "GetGalleriesCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request): for date " + this.getDate());
		}
		return toString;
	}
}
