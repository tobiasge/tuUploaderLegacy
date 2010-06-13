package com.teamulm.uploadsystem.protocol;

import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.data.Gallery;

public abstract class PathCmd extends Command {

	public static final int ERROR_LOC_BADLOC = 2;

	public static final int ERROR_LOC_NOTFREE = 1;

	private static final long serialVersionUID = -3841983645791239967L;

	private LocalDate date;

	private String location;

	private int startNumber;

	private int suffix;

	protected PathCmd(CommandType type) {
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
}
