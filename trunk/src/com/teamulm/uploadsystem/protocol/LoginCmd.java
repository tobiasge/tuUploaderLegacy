package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class LoginCmd extends Command {

	private static final long serialVersionUID = -3858622134307879471L;

	private String passWord;

	private String userName;

	public LoginCmd() {
		super();
	}

	public LoginCmd(CommandType type) {
		super(type);
	}

	public String getPassWord() {
		return this.passWord;
	}

	public String getUserName() {
		return this.userName;
	}

	public void setPassWord(String passWord) {
		this.passWord = passWord;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"UserName", this.userName).toString(); //$NON-NLS-1$
	}
}
