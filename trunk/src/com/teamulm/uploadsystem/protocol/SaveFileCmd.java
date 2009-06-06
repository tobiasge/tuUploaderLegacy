package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class SaveFileCmd extends Command {

	private static final long serialVersionUID = 2205745072650594896L;

	private String fileName;

	private int fileSize;

	private byte[] fileContent;

	public SaveFileCmd() {
		super();
	}

	public SaveFileCmd(boolean serverResponse) {
		super(serverResponse);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileSize() {
		return fileSize;
	}

	public void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}

	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"FileSize", this.fileSize).append("FileName", this.fileName).toString(); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
