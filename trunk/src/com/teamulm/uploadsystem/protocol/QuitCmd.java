package com.teamulm.uploadsystem.protocol;

public class QuitCmd extends Command {

	private static final long serialVersionUID = 4296633670078187691L;

	public QuitCmd() {
		super();
	}

	public QuitCmd(boolean serverResponse) {
		super(serverResponse);
	}
}
