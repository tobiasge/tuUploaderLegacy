package com.teamulm.uploadsystem.client.layout.comp;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
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
		this.setTransferHandler(new FileTransferHandler());
		this.setDropMode(DropMode.ON);
		this.setDragEnabled(true);
	}

	public void setListPictureData(File[] f) {
		// Verhindert doppelte Einträge
		Vector<File> tmp = new Vector<File>();
		Collections.addAll(tmp, f);
		this.setListPictureData(tmp);
	}

	public void setListPictureData(Vector<File> f) {
		this.fileList.removeAll(f);
		this.fileList.addAll(f);
		Collections.sort(this.fileList, new FileNameSorter());
		this.updateListPicture();
	}

	private void updateListPicture() {
		this.files.removeAllElements();
		Iterator<File> fileIt = this.fileList.iterator();
		while (fileIt.hasNext())
			this.files.addElement(fileIt.next());
	}

	public void clearAllFiles() {
		this.fileList.clear();
		this.updateListPicture();
	}

	public void removeSelectedListPictureData() {
		Object[] indices = this.getSelectedValues();
		for (Object tmp : indices)
			this.fileList.remove(tmp);
		this.updateListPicture();
	}

	public File[] getFiles() {
		File[] files = new File[this.fileList.size()];
		this.fileList.toArray(files);
		return files;
	}

	private class FileNameSorter implements Comparator<File> {

		public FileNameSorter() {
		}

		public int compare(File o1, File o2) {
			try {
				return o1.getAbsoluteFile().getCanonicalPath().compareTo(
						o2.getAbsoluteFile().getCanonicalPath());
			} catch (IOException e) {
				Helper.getInstance().systemCrashHandler(e);
				return 0;
			}
		}
	}

	private class FileTransferHandler extends TransferHandler {

		private final Logger log = Logger.getLogger(FileTransferHandler.class);

		private DataFlavor fileFlavor = DataFlavor.javaFileListFlavor;

		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			return this.hasFileFlavor(transferFlavors);
		}

		private boolean hasFileFlavor(DataFlavor[] flavors) {
			for (int i = 0; i < flavors.length; i++) {
				if (this.fileFlavor.equals(flavors[i])) {
					return true;
				}
			}
			return false;
		}

		@Override
		public int getSourceActions(JComponent c) {
			return TransferHandler.COPY;
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			List<?> tmpList = null;
			Vector<File> tmpFileList = new Vector<File>();
			if (!this.hasFileFlavor(t.getTransferDataFlavors()))
				return false;
			try {
				if (t.getTransferData(this.fileFlavor) instanceof List<?>) {
					tmpList = (List<?>) t.getTransferData(this.fileFlavor);
				} else {
					return false;
				}
				Iterator<?> it = tmpList.iterator();
				while (it.hasNext()) {
					Object tmp = it.next();
					if (tmp instanceof File) {
						File tmpFile = (File) tmp;
						if (tmpFile.getName().endsWith(".jpg")
								|| tmpFile.getName().endsWith(".jpeg")
								|| tmpFile.getName().endsWith(".JPG")
								|| tmpFile.getName().endsWith(".JPEG")) {
							tmpFileList.add(tmpFile);
						} else {
							log.warn("DnD: Ignored file " + tmpFile.getName());
						}
					}
				}
			} catch (IOException ioe) {
				this.log.error(ioe.getMessage());
			} catch (UnsupportedFlavorException ufe) {
				this.log.error(ufe.getMessage());
			}
			FileList.this.setListPictureData(tmpFileList);
			return true;
		}
	}
}
