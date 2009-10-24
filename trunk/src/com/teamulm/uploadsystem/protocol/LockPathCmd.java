package com.teamulm.uploadsystem.protocol;

public class LockPathCmd extends PathCmd {

	private static final long serialVersionUID = -3577876577857408464L;

	public LockPathCmd(CommandType type) {
		super(type);
	}

	public String toString() {
		String toString = "LockPathCmd "; //$NON-NLS-1$
		if (CommandType.RESPONSE == this.getType()) {
			toString = toString.concat("(Response): commandSucceded() = " + this.commandSucceded()); //$NON-NLS-1$
		} else {
			toString = toString.concat("(Request): for path " + this.getPath()); //$NON-NLS-1$
		}
		return toString;
	}
}
