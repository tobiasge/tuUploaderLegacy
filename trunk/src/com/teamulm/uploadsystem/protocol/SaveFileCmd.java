package com.teamulm.uploadsystem.protocol;

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
		String toString = "SaveFileCmd ";
		if (this.isServerResponse()) {
			toString = toString.concat("(Response): commandSucceded() = "
					+ this.commandSucceded());
		} else {
			toString = toString.concat("(Request)");
		}
		return toString;
	}
}
