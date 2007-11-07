package com.teamulm.uploadsystem.protocol;

import com.teamulm.uploadsystem.data.Gallery;

public class SaveGalleryCmd extends Command {

	private static final long serialVersionUID = 9200426083996725517L;

	private Gallery gallery;

	public SaveGalleryCmd() {
		super();
	}

	public SaveGalleryCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public String toString() {
		String toString = "SaveGalleryCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request)");
		}
		return toString;
	}
}
