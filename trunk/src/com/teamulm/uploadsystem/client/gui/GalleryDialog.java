package com.teamulm.uploadsystem.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import javax.swing.JCheckBox;
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
import com.teamulm.uploadsystem.client.gui.comp.MyJTextField;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class GalleryDialog extends JDialog {

	private static final long serialVersionUID = -9193707556220629559L;
	private static boolean locationsLoaded = false;
	private ArrayList<Gallery> myGalleries;
	private DefaultTableModel galTableModel;
	private MyJComboBox locationsBox;
	private MyJTextField titleField, descField;
	private JTable galTable;
	private String date;

	public GalleryDialog(String date) {
		super(MainWindow.getInstance(), "Galerien vom "
				+ date.replace('-', '.'), true);
		this.date = date;
		this.setPreferredSize(new Dimension(450, 260));
		this.setMinimumSize(new Dimension(450, 260));
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

		JScrollPane scroller = new JScrollPane(this.galTable);
		scroller.setPreferredSize(new Dimension(428, 120));
		scroller.setMinimumSize(new Dimension(428, 120));
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
				"Schon vorhandene Galerie wählen oder neue Galerie erstellen:");
		this.add(oldGal, constraints);
		constraints.insets = new Insets(4, 2, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(scroller, constraints);
		new GalleryLoader(this.date, this).execute();

		constraints.gridx = 0;
		constraints.gridy = 2;
		this.add(this.buildNewGalPanel(), constraints);

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

	private JPanel buildNewGalPanel() {
		JPanel newPanel = new JPanel();
		newPanel.setPreferredSize(new Dimension(428, 80));
		newPanel.setMinimumSize(new Dimension(428, 80));
		newPanel.setLayout(new GridBagLayout());
		JPanel line1Panel = new JPanel();
		line1Panel.setPreferredSize(new Dimension(428, 24));
		line1Panel.setMinimumSize(new Dimension(428, 24));
		line1Panel.setLayout(new GridBagLayout());
		GridBagConstraints cons = new GridBagConstraints();
		cons.anchor = GridBagConstraints.LINE_START;
		cons.gridx = 0;
		cons.gridy = 0;
		cons.insets = new Insets(0, 0, 0, 5);
		this.locationsBox = new MyJComboBox();
		new LocationsLoader(this.locationsBox).execute();
		line1Panel.add(this.locationsBox, cons);

		cons.gridx = 1;
		cons.gridy = 0;
		cons.insets = new Insets(0, 0, 0, 70);
		this.titleField = new MyJTextField(100);
		this.titleField.setPreferredSize(new Dimension(170, 20));
		this.titleField.setMinimumSize(new Dimension(170, 20));
		this.titleField.setForeground(Color.GRAY);
		this.titleField.setText("Titel");
		this.titleField.addFocusListener(new TextFieldFocus("Titel"));
		line1Panel.add(this.titleField, cons);

		cons.gridx = 2;
		cons.gridy = 0;
		cons.anchor = GridBagConstraints.LINE_END;
		cons.insets = new Insets(0, 0, 0, 42);
		line1Panel.add(new JCheckBox(), cons);

		cons.gridx = 0;
		cons.gridy = 0;
		cons.anchor = GridBagConstraints.LINE_START;
		cons.insets = new Insets(4, 2, 0, 2);
		newPanel.add(line1Panel, cons);

		cons.gridx = 0;
		cons.gridy = 1;
		this.descField = new MyJTextField(1000);
		this.descField.setForeground(Color.GRAY);
		this.descField.setText("Beschreibung");
		this.descField.addFocusListener(new TextFieldFocus("Beschreibung"));
		this.descField.setPreferredSize(new Dimension(428, 20));
		this.descField.setMinimumSize(new Dimension(428, 20));
		newPanel.add(this.descField, cons);

		cons.gridx = 0;
		cons.gridy = 2;
		cons.anchor = GridBagConstraints.LINE_END;
		newPanel.add(new MyJButton("Übernehmen"), cons);

		return newPanel;
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

	private class LocationsLoader extends SwingWorker<byte[], Void> {

		private MyJComboBox locationsBox;
		private String fileName;

		public LocationsLoader(MyJComboBox locationsBox) {
			this.locationsBox = locationsBox;
			this.fileName = Helper.getInstance().getFileLocation(
					"locations.list");
		}

		@Override
		protected byte[] doInBackground() throws Exception {
			byte[] contentByteArray = null;
			if (GalleryDialog.locationsLoaded)
				return null;
			try {

				URLConnection locationsURL = new URL(
						"http://www.team-ulm.de/fotos/locations.php")
						.openConnection();
				int contentLength = locationsURL.getContentLength();
				contentByteArray = new byte[contentLength];
				InputStream fromServer = locationsURL.getInputStream();
				int readBytes = fromServer.read(contentByteArray);
				if (readBytes != contentLength)
					throw new Exception("Content not fully read");
				MainWindow.getInstance().addStatusLine(
						"Locations Update fertig");
				GalleryDialog.locationsLoaded = true;
			} catch (IOException ioEx) {
				MainWindow.getInstance().addStatusLine(
						"Konnte Liste nicht updaten");
				Helper.getInstance().systemCrashHandler(ioEx);
				return null;
			}
			try {
				FileOutputStream out = new FileOutputStream(fileName);
				out.write(contentByteArray);
				out.flush();
				out.close();
			} catch (IOException ioEx) {
				MainWindow.getInstance().addStatusLine(
						"Konnte Liste nicht speichern");
				Helper.getInstance().systemCrashHandler(ioEx);
			}
			return contentByteArray;
		}

		@Override
		protected void done() {
			byte[] locations;
			try {
				locations = this.get();
				if (null == locations) {
					this.locationsBox.setLocations(this.fileName);
				} else {
					this.locationsBox.setLocations(locations);
				}
			} catch (ExecutionException executionException) {
			} catch (InterruptedException interruptedException) {
			}
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

	private class TextFieldFocus implements FocusListener {

		private String defaultString = "";

		TextFieldFocus(String defaultString) {
			this.defaultString = defaultString;
		}

		public void focusGained(FocusEvent e) {
			if (!(e.getSource() instanceof MyJTextField))
				return;
			MyJTextField field = (MyJTextField) e.getSource();
			if (field.getText().equalsIgnoreCase(defaultString)) {
				field.setText("");
				field.setForeground(Color.BLACK);
			}
		}

		public void focusLost(FocusEvent e) {
			if (!(e.getSource() instanceof MyJTextField))
				return;
			MyJTextField field = (MyJTextField) e.getSource();
			if (field.getText().equalsIgnoreCase("")
					|| field.getText().equalsIgnoreCase(this.defaultString)) {
				field.setForeground(Color.GRAY);
				field.setText(this.defaultString);
			}
		}
	}
}
