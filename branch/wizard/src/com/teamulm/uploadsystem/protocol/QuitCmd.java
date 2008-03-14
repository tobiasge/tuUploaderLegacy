package com.teamulm.uploadsystem.protocol;

public class QuitCmd extends Command {

	private static final long serialVersionUID = 4296633670078187691L;

	public QuitCmd() {
		super();
	}

	public QuitCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public String toString() {
		String toString = "QuitCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request)");
		}
		return toString;
	}
}
