package com.teamulm.uploadsystem.protocol;

public class UnLockPathCmd extends Command {

	private static final long serialVersionUID = 8073402000599416724L;

	private String location;

	private String date;

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

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
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
			return this.location + System.getProperty("file.separator")
					+ this.date + System.getProperty("file.separator");
		} else {
			return this.location + System.getProperty("file.separator")
					+ this.date + "-" + this.suffix
					+ System.getProperty("file.separator");
		}
	}

	public String toString() {
		String toString = "UnLockPathCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
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
