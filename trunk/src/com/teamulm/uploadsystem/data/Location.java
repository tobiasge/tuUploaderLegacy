package com.teamulm.uploadsystem.data;

import java.io.Serializable;

public class Location implements Serializable {

	private static final long serialVersionUID = -4083430320962596602L;

	private int id = -1;

	private String name = null;

	public Location(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
