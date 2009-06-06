package com.teamulm.uploadsystem.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class Command implements Serializable {

	private static final long serialVersionUID = 5593609768805163288L;

	public static final int ERROR_LOC_NOTFREE = 1;

	public static final int ERROR_LOC_BADLOC = 2;

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

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("ServerResponse", this.serverResponse).append("Success", this.success).toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
