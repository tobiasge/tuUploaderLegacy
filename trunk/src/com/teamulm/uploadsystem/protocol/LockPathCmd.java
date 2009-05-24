package com.teamulm.uploadsystem.protocol;

import java.util.Date;

public class LockPathCmd extends Command {

	private static final long serialVersionUID = -8628939768509360791L;

	public static final int ERROR_LOC_NOTFREE = 1;

	public static final int ERROR_LOC_BADLOC = 2;

	private String location;

	private Date date;

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

	public Date getDate() {
		return this.date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getStartNumber() {
		return startNumber;
	}

	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
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
