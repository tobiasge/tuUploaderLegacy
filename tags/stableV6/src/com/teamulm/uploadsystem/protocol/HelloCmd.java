package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class HelloCmd extends Command {

	private static final long serialVersionUID = 5503474416981045766L;

	private String protocolVersionString = "1.0"; //$NON-NLS-1$

	public HelloCmd(CommandType type) {
		super(type);
	}

	public String getProtocolVersionString() {
		return this.protocolVersionString;
	}

	public void setProtocolVersionString(String protocolVersionString) {
		this.protocolVersionString = protocolVersionString;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"ProtocolVersionString", this.protocolVersionString).toString(); //$NON-NLS-1$
	}
}
