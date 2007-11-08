package com.teamulm.uploadsystem.client.layout.comp;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.listener.MyJListListener;
import com.teamulm.uploadsystem.client.listener.kl.KLPicturesList;

@SuppressWarnings("serial")
public class FileList extends JList {
	private DefaultListModel files;

	private Vector<File> fileList;

	public FileList() {
		super();
		this.fileList = new Vector<File>();
		this.files = new DefaultListModel();
		this.files.addListDataListener(new MyJListListener());
		this.addKeyListener(new KLPicturesList());
		this.setModel(this.files);
	}

	public void setListPictureData(File[] f) {
		// Verhindert doppelte Einträge
		Vector<File> tmp = new Vector<File>();
		Collections.addAll(tmp, f);
		this.fileList.removeAll(tmp);
		this.fileList.addAll(tmp);
		Collections.sort(this.fileList, FileNameSorter.getInstance());
		updateListPicture();
	}

	private void updateListPicture() {
		this.files.removeAllElements();
		Iterator<File> fileIt = this.fileList.iterator();
		while (fileIt.hasNext())
			this.files.addElement(fileIt.next());
	}

	public void clearAllFiles() {
		this.fileList.clear();
		updateListPicture();
	}

	public void removeSelectedListPictureData() {
		Object[] indices = this.getSelectedValues();
		for (Object tmp : indices)
			this.fileList.remove(tmp);
		updateListPicture();
	}

	public File[] getFiles() {
		File[] files = new File[this.fileList.size()];
		this.fileList.toArray(files);
		return files;
	}

}

class FileNameSorter implements Comparator<File> {
	private static FileNameSorter instance;

	public static FileNameSorter getInstance() {
		if (null == FileNameSorter.instance) {
			FileNameSorter.instance = new FileNameSorter();
		}
		return FileNameSorter.instance;
	}

	private FileNameSorter() {
	}

	public int compare(File o1, File o2) {
		try {
			return o1.getAbsoluteFile().getCanonicalPath().compareTo(
					o2.getAbsoluteFile().getCanonicalPath());
		} catch (IOException e) {
			TeamUlmUpload.getInstance().systemCrashHandler(e);
			return 0;
		}
	}
}
