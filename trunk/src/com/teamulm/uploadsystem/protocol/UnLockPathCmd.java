package com.teamulm.uploadsystem.protocol;

import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.data.Gallery;

public class UnLockPathCmd extends Command {

	private static final long serialVersionUID = 8073402000599416724L;

	private String location;

	private LocalDate date;

	private int suffix;

	private int startNumber;

	public UnLockPathCmd() {
		super();
	}

	public UnLockPathCmd(boolean serverResponse) {
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
		String toString = "UnLockPathCmd ";
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
