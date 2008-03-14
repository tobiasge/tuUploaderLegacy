package com.teamulm.uploadsystem.protocol;

import java.io.Serializable;

public abstract class Command implements Serializable {

	private static final long serialVersionUID = 5593609768805163288L;

	private boolean serverResponse;

	private boolean success;

	private String errorMsg;

	private int errorCode;

	protected Command() {
		this.serverResponse = false;
		this.success = false;
	}

	protected Command(boolean serverResponse) {
		this.serverResponse = serverResponse;
		this.success = false;
	}

	public boolean isServerResponse() {
		return serverResponse;
	}

	public void setServerResponse(boolean serverResponse) {
		this.serverResponse = serverResponse;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public boolean commandSucceded() {
		return this.isServerResponse() && this.success;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}
}
