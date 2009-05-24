package com.teamulm.uploadsystem.protocol;

import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.data.Gallery;

public class LockPathCmd extends Command {

	private static final long serialVersionUID = -8628939768509360791L;

	public static final int ERROR_LOC_NOTFREE = 1;

	public static final int ERROR_LOC_BADLOC = 2;

	private String location;

	private LocalDate date;

	private int suffix;

	private int startNumber;

	public LockPathCmd() {
		super();
	}

	public LockPathCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public LocalDate getDate() {
		return this.date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public int getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
	}

	public String getPath() {
		return Gallery.getPath(this.location, this.date, this.suffix);
	}

	public String toString() {
		String toString = "LockPathCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = " + this.commandSucceded());
		} else {
			toString = toString.concat("(Request): for path " + this.getPath());
		}
		return toString;
	}

	public int getSuffix() {
		return suffix;
	}

	public void setSuffix(int suffix) {
		this.suffix = suffix;
	}
}
