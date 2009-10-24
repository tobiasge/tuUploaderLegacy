package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class AuthenticationCmd extends Command {

	private static final long serialVersionUID = 527671034057872163L;

	private String message;

	public AuthenticationCmd(CommandType type) {
		super(type);
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"Message", this.message).toString(); //$NON-NLS-1$
	}
}
