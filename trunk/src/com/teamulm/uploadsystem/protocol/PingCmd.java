package com.teamulm.uploadsystem.protocol;

public class PingCmd extends Command {

	private static final long serialVersionUID = 488184457320981227L;

	private long millis;
	
	public PingCmd() {
		super();
		this.millis = System.currentTimeMillis();
	}

	public PingCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public String toString() {
		String toString = "PingCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request) at " + this.millis);
		}
		return toString;
	}
}
