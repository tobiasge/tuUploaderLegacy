package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class PingCmd extends Command {

	private static final long serialVersionUID = 488184457320981227L;

	private long millis;

	public PingCmd(CommandType type) {
		super(type);
		this.millis = System.currentTimeMillis();
	}

	public long getMillis() {
		return this.millis;
	}

	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"Millis", this.millis).toString(); //$NON-NLS-1$
	}
}
