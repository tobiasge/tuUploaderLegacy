package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"UserName", this.userName).toString();
	}
}
