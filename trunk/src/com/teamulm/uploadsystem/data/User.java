package com.teamulm.uploadsystem.data;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class User implements Serializable {

	private static final long serialVersionUID = 6277405439383266829L;

	private String password;

	private int userid;

	private String username;

	public User(int userID, String passWord, String userName) {
		this.userid = userID;
		this.password = passWord;
		this.username = userName;
	}

	public String getPassword() {
		return password;
	}

	public int getUserid() {
		return userid;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUserid(int userid) {
		this.userid = userid;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("UserId", this.userid).append( //$NON-NLS-1$
			"UserName", this.username).toString(); //$NON-NLS-1$
	}
}
