package com.teamulm.uploadsystem.protocol;

public class LockPathCmd extends Command {

	private static final long serialVersionUID = -8628939768509360791L;

	private String location;

	private String date;

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
		return this.location + System.getProperty("file.separator") + this.date
				+ System.getProperty("file.separator");
	}

	public String toString() {
		String toString = "LockPathCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request): for path " + this.getPath());
		}
		return toString;
	}
}
