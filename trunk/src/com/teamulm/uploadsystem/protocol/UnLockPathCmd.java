package com.teamulm.uploadsystem.protocol;

public class UnLockPathCmd extends PathCmd {

	private static final long serialVersionUID = 8073402000599416724L;

	public UnLockPathCmd(CommandType type) {
		super(type);
	}

	@Override
	public String toString() {
		String toString = "UnLockPathCmd "; //$NON-NLS-1$
		if (CommandType.RESPONSE == this.getType()) {
			toString = toString.concat("(Response): commandSucceded() = " + this.commandSucceded()); //$NON-NLS-1$
		} else {
			toString = toString.concat("(Request): for path " + this.getPath()); //$NON-NLS-1$
		}
		return toString;
	}
}
