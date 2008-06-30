package com.teamulm.uploadsystem.client.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.comp.MyJButton;
import com.teamulm.uploadsystem.client.gui.comp.MyJComboBox;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class GalleryDialog extends JDialog {

	private static final long serialVersionUID = -9193707556220629559L;

	private ArrayList<Gallery> myGalleries;
	private DefaultTableModel galTableModel;
	private MyJComboBox locationsBox;
	private JTable galTable;
	private String date;

	public GalleryDialog(String date) {
		super(MainWindow.getInstance(), "Galerie auswählen", true);
		this.date = date;
		this.setPreferredSize(new Dimension(450, 230));
		this.setMinimumSize(new Dimension(450, 230));
		this.setResizable(false);

		this.setLayout(new GridBagLayout());

		String[] header = { "Location", "Titel", "Bilder", "Intern" };
		this.galTableModel = new DefaultTableModel(header, 0) {

			private static final long serialVersionUID = -6260891718038382496L;

			@Override
			public boolean isCellEditable(int arg0, int arg1) {
				return false;
			}
		};
		this.galTable = new JTable() {

			private static final long serialVersionUID = 0L;

			@Override
			public Class<?> getColumnClass(int column) {
				if (null != getValueAt(0, column))
					return getValueAt(0, column).getClass();
				return String.class;
			}
		};

		this.galTable.setModel(this.galTableModel);
		this.galTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.galTable.setColumnSelectionAllowed(false);
		this.galTable.setRowSelectionAllowed(true);
		this.galTable.getTableHeader().setReorderingAllowed(false);
		this.galTable.getTableHeader().setResizingAllowed(false);
		this.galTable.getColumnModel().setColumnMargin(7);
		this.setColumnWidth(0, 140);
		this.setColumnWidth(1, 170);
		this.setColumnWidth(2, 50);
		this.setColumnWidth(3, 50);

		this.galTable.addMouseListener(new OldGalleryListener());
		this.galTable.setToolTipText("Per Doppelklick auswählen");

		JScrollPane scroller = new JScrollPane(this.galTable) {
			private static final long serialVersionUID = 0L;

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(428, 120);
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(428, 120);
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(428, 120);
			}
		};
		scroller
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.insets = new Insets(0, 2, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.LINE_START;
		JLabel oldGal = new JLabel(
				"Schon vorhandene Galerie wählen (mit Doppelklick):");
		oldGal.setToolTipText("Per Doppelklick auswählen");
		this.add(oldGal, constraints);
		constraints.insets = new Insets(4, 2, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(scroller, constraints);
		new GalleryLoader(this.date, this).execute();

		JPanel buttonPanel = new JPanel(new GridBagLayout());
		constraints.gridy = 0;
		constraints.gridx = 0;
		buttonPanel.add(new JLabel("Neue Galerie erstellen:"), constraints);
		constraints.gridy = 1;
		constraints.gridx = 0;

		locationsBox = new MyJComboBox();

		locationsBox
				.setToolTipText("Hier die Location für eine neue Galerie wählen.");
		constraints.gridy = 1;
		constraints.gridx = 1;
		buttonPanel.add(locationsBox, constraints);
		new LocationsLoader(locationsBox).execute();
		constraints.gridy = 1;
		constraints.gridx = 2;
		JButton newButton = new MyJButton("Galerie erstellen");
		newButton.addActionListener(new NewGalleryListener());
		newButton
				.setToolTipText("Erstellt eine neue Galerie in der gewählten Location.");
		buttonPanel.add(newButton, constraints);

		constraints.insets = new Insets(4, 0, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.add(buttonPanel, constraints);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width - getSize().width) / 2,
				(d.height - getSize().height) / 2);

		this.setVisible(true);
	}

	public MyJComboBox getLocationsBox() {
		return this.locationsBox;
	}

	private void showGalleries() {
		this.galTableModel.setRowCount(0);
		for (Gallery gal : this.myGalleries) {
			this.galTableModel
					.addRow(new Object[] { gal, gal.getTitle(),
							new Integer(gal.getPictures()),
							new Boolean(gal.isIntern()) });
		}
	}

	public void setGalleries(ArrayList<Gallery> galList) {
		this.myGalleries = galList;
	}

	private void setColumnWidth(int column, int width) {
		this.galTable.getColumnModel().getColumn(column).setPreferredWidth(
				width);
		this.galTable.getColumnModel().getColumn(column).setMaxWidth(width);
		this.galTable.getColumnModel().getColumn(column).setMinWidth(width);
	}

	private class GallerySorter implements Comparator<Gallery> {
		public int compare(Gallery o1, Gallery o2) {
			int retVal = o1.getLocation().compareTo(o2.getLocation());
			if (0 == retVal) {
				return o1.getTitle().compareTo(o2.getTitle());
			} else
				return retVal;
		}
	}

	private class NewGalleryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (GalleryDialog.this.getLocationsBox().getSelectedItem() instanceof String
					&& ((String) GalleryDialog.this.locationsBox
							.getSelectedItem())
							.compareTo("    -- Bitte wählen --") != 0) {
				MainWindow.getInstance().setGallery(
						TrmEngine.getInstance().newGallery(
								(String) GalleryDialog.this.locationsBox
										.getSelectedItem(), date));
				GalleryDialog.this.dispose();
			} else {
				JOptionPane.showMessageDialog(GalleryDialog.this,
						"Bitte eine Location auswählen!", "Location...",
						JOptionPane.ERROR_MESSAGE, null);
			}
		}
	}

	private class OldGalleryListener extends MouseAdapter {
		@Override
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() == 2)
				this.selectOld();
		}

		private void selectOld() {
			if (-1 == GalleryDialog.this.galTable.getSelectedRow()) {
				JOptionPane.showMessageDialog(GalleryDialog.this,
						"Bitte eine Galerie auswählen!", "Galerie...",
						JOptionPane.ERROR_MESSAGE, null);
			} else {
				if (GalleryDialog.this.galTable.getValueAt(
						GalleryDialog.this.galTable.getSelectedRow(), 0) instanceof Gallery) {
					MainWindow.getInstance().setGallery(
							(Gallery) GalleryDialog.this.galTable.getValueAt(
									GalleryDialog.this.galTable
											.getSelectedRow(), 0));
					GalleryDialog.this.dispose();
				} else {
					JOptionPane.showMessageDialog(GalleryDialog.this,
							"Bitte eine Galerie auswählen!", "Galerie...",
							JOptionPane.ERROR_MESSAGE, null);
				}
			}
		}
	}

	private class LocationsLoader extends SwingWorker<Void, Void> {

		private MyJComboBox locationsBox;
		private String fileName;

		public LocationsLoader(MyJComboBox locationsBox) {
			this.locationsBox = locationsBox;
			this.fileName = Helper.getInstance().getFileLocation(
					"locations.list");
		}

		@Override
		protected Void doInBackground() throws Exception {
			try {

				URLConnection locationsURL = new URL(
						"http://www.team-ulm.de/fotos/locations.php")
						.openConnection();
				int contentLength = locationsURL.getContentLength();
				byte[] contentByteArray = new byte[contentLength];
				InputStream fromServer = locationsURL.getInputStream();
				int readBytes = fromServer.read(contentByteArray);
				if (readBytes != contentLength)
					throw new Exception("Content not fully read");
				FileOutputStream out = new FileOutputStream(fileName);
				out.write(contentByteArray);
				out.flush();
				out.close();
			} catch (IOException ioEx) {
				MainWindow.getInstance().addStatusLine(
						"Konnte Liste nicht updaten");
				Helper.getInstance().systemCrashHandler(ioEx);
			}
			return null;
		}

		@Override
		protected void done() {
			this.locationsBox.setLocationsFile(this.fileName);
			MainWindow.getInstance().addStatusLine("Locations Update fertig");
			super.done();
		}
	}

	private class GalleryLoader extends SwingWorker<ArrayList<Gallery>, Void> {

		private String date;
		private GalleryDialog parent;

		public GalleryLoader(String date, GalleryDialog parent) {
			this.date = date;
			this.parent = parent;
		}

		@Override
		protected ArrayList<Gallery> doInBackground() throws Exception {
			ArrayList<Gallery> galleryList = TrmEngine.getInstance()
					.getGalleriesFor(this.date);
			return galleryList;
		}

		@Override
		protected void done() {
			try {
				ArrayList<Gallery> list = this.get();
				Collections.sort(list, new GallerySorter());
				this.parent.setGalleries(list);
				this.parent.showGalleries();
				MainWindow.getInstance().addStatusLine("Galerien laden fertig");
			} catch (ExecutionException executionException) {

			} catch (InterruptedException interruptedException) {

			}
		}
	}
}
