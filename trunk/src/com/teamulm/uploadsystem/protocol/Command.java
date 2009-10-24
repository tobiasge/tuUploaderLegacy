package com.teamulm.uploadsystem.protocol;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public abstract class Command implements Serializable {

	public static final int ERROR_LOC_BADLOC = 2;

	public static final int ERROR_LOC_NOTFREE = 1;

	private static final long serialVersionUID = 5593609768805163288L;

	private int errorCode;

	private String errorMsg;

	private boolean success;

	private CommandType type = null;

	protected Command(CommandType type) {
		this.type = type;
		this.success = false;
	}

	public boolean commandSucceded() {
		return CommandType.RESPONSE == this.type && this.success;
	}

	public int getErrorCode() {
		return this.errorCode;
	}

	public String getErrorMsg() {
		return this.errorMsg;
	}

	public CommandType getType() {
		return this.type;
	}

	public boolean isSuccess() {
		return this.success;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
			.append("Type", this.type).append("Success", this.success).toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static enum CommandType {
		REQUEST, RESPONSE;
	}
}
