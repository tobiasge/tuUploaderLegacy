package com.teamulm.uploadsystem.protocol;

public class HelloCmd extends Command {

	private static final long serialVersionUID = 5503474416981045766L;

	private String protocolVersionString = "1.0";

	public HelloCmd() {
		super();
	}

	public HelloCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public String getProtocolVersionString() {
		return protocolVersionString;
	}

	public void setProtocolVersionString(String protocolVersionString) {
		this.protocolVersionString = protocolVersionString;
	}

	public String toString() {
		String toString = "HelloCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request): protocolVersion = "
					+ this.protocolVersionString);
		}
		return toString;
	}
}
