package com.teamulm.uploadsystem.protocol;

public class AuthenticationCmd extends Command {

	private static final long serialVersionUID = 527671034057872163L;

	private String message;

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public AuthenticationCmd() {
		super();
	}

	public AuthenticationCmd(boolean serverResponse) {
		super(serverResponse);
	}
}
