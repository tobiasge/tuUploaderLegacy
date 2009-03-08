package com.teamulm.uploadsystem.protocol;

import java.util.List;

import com.teamulm.uploadsystem.data.Location;

public class GetLocationsCmd extends Command {

	private static final long serialVersionUID = 4044374543826008958L;

	private List<Location> locations = null;

	public GetLocationsCmd() {
		super();
	}

	public GetLocationsCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public List<Location> getLocations() {
		return locations;
	}

	public void setLocations(List<Location> locations) {
		this.locations = locations;
	}

}
