package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class SaveFileCmd extends Command {

	private static final long serialVersionUID = 2205745072650594896L;

	private byte[] fileContent;

	private String fileName;

	private int fileSize;

	public SaveFileCmd(CommandType type) {
		super(type);
	}

	public byte[] getFileContent() {
		return this.fileContent;
	}

	public String getFileName() {
		return this.fileName;
	}

	public int getFileSize() {
		return this.fileSize;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"FileSize", this.fileSize).append("FileName", this.fileName).toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
