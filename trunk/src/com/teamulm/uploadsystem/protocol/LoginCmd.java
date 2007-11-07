package com.teamulm.uploadsystem.protocol;

public class LoginCmd extends Command {

	private static final long serialVersionUID = -3858622134307879471L;

	private String userName;

	private String passWord;

	public LoginCmd() {
		super();
	}

	public LoginCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassWord() {
		return passWord;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public String toString() {
		String toString = "LoginCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request)");
		}
		return toString;
	}
}
