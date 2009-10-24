package com.teamulm.uploadsystem.protocol;

import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.data.Gallery;

public class UnLockPathCmd extends Command {

	private static final long serialVersionUID = 8073402000599416724L;

	private LocalDate date;

	private String location;

	private int startNumber;

	private int suffix;

	public UnLockPathCmd(CommandType type) {
		super(type);
	}

	public LocalDate getDate() {
		return this.date;
	}

	public String getLocation() {
		return this.location;
	}

	public String getPath() {
		return Gallery.getPath(this.location, this.date, this.suffix);
	}

	public int getStartNumber() {
		return this.startNumber;
	}

	public int getSuffix() {
		return this.suffix;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public void setStartNumber(int startNumber) {
		this.startNumber = startNumber;
	}

	public void setSuffix(int suffix) {
		this.suffix = suffix;
	}

	public String toString() {
		String toString = "UnLockPathCmd "; //$NON-NLS-1$
		if (CommandType.RESPONSE == this.getType()) {
			toString = toString.concat("(Response): commandSucceded() = " + this.commandSucceded()); //$NON-NLS-1$
		} else {
			toString = toString.concat("(Request): for path " + this.getPath()); //$NON-NLS-1$
		}
		return toString;
	}
}
