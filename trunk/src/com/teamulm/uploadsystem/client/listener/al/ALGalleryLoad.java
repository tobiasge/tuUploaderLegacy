package com.teamulm.uploadsystem.client.listener.al;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;
import com.teamulm.uploadsystem.client.gui.comp.MyJButton;
import com.teamulm.uploadsystem.client.gui.comp.MyJComboBox;
import com.teamulm.uploadsystem.client.gui.comp.UserPassDialog;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class ALGalleryLoad implements ActionListener {

	private static final Logger log = Logger.getLogger(ALGalleryLoad.class);

	public void actionPerformed(ActionEvent arg0) {
		log.debug("starting gallery selection");

		if (!TrmEngine.getInstance().isConnected()) {
			if (!TrmEngine.getInstance().connect()) {
				MainWindow.getInstance().addStatusLine(
						"Konnte keine Verbindung herstellen");
				return;
			}
		}
		if (!TrmEngine.getInstance().isLoggedIn()) {
			UserPassDialog dialog = new UserPassDialog();
			int userSaidValue = dialog.passDialog();
			if (JOptionPane.NO_OPTION == userSaidValue
					|| JOptionPane.CLOSED_OPTION == userSaidValue) {
				MainWindow.getInstance().addStatusLine("Abgebrochen.");
				return;
			}
			log.debug("starte upload " + userSaidValue);
			if (!TrmEngine.getInstance().login(dialog.getUser(),
					dialog.getPass())) {
				MainWindow.getInstance().addStatusLine(
						"Username oder Passwort falsch.");
				return;
			}
		}

		ArrayList<Gallery> galleryList = TrmEngine.getInstance()
				.getGalleriesFor(
						MainWindow.getInstance().getDateEditor()
								.getDateString());
		log.debug("Found " + galleryList.size() + " galleries");

		new GalleryDialog(galleryList, MainWindow.getInstance().getDateEditor()
				.getDateString());
	}

	private class GalleryDialog extends JDialog {

		private static final long serialVersionUID = -9193707556220629559L;

		private ArrayList<Gallery> myGalleries;
		private DefaultTableModel galTableModel;
		private MyJComboBox locationsBox;
		private JTable galTable;
		private String date;

		public GalleryDialog(ArrayList<Gallery> galleries, String date) {
			super(MainWindow.getInstance(), "Galerie auswählen", true);
			this.myGalleries = galleries;
			this.date = date;
			Collections.sort(this.myGalleries, new GallerySorter());
			this.setPreferredSize(new Dimension(450, 190));
			this.setMinimumSize(new Dimension(450, 190));
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
			// this.galTable.getSelectionModel().addListSelectionListener(
			// new GalleryListener());
			this.galTable.setModel(this.galTableModel);
			this.galTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			this.galTable.setColumnSelectionAllowed(false);
			this.galTable.setRowSelectionAllowed(true);
			this.galTable.getTableHeader().setReorderingAllowed(false);
			this.galTable.getTableHeader().setResizingAllowed(false);
			this.galTable.getColumnModel().getColumn(0).setPreferredWidth(120);
			this.galTable.getColumnModel().getColumn(0).setMaxWidth(120);
			this.galTable.getColumnModel().getColumn(0).setMinWidth(120);

			this.galTable.getColumnModel().getColumn(1).setPreferredWidth(150);
			this.galTable.getColumnModel().getColumn(1).setMaxWidth(150);
			this.galTable.getColumnModel().getColumn(1).setMinWidth(150);

			this.galTable.getColumnModel().getColumn(2).setPreferredWidth(50);
			this.galTable.getColumnModel().getColumn(2).setMaxWidth(50);
			this.galTable.getColumnModel().getColumn(2).setMinWidth(50);

			this.galTable.getColumnModel().getColumn(3).setPreferredWidth(50);
			this.galTable.getColumnModel().getColumn(3).setMaxWidth(50);
			this.galTable.getColumnModel().getColumn(3).setMinWidth(50);

			this.galTable.addMouseListener(new OldGalleryListener());

			JScrollPane scroller = new JScrollPane(this.galTable) {
				private static final long serialVersionUID = 0L;

				@Override
				public Dimension getPreferredSize() {
					return new Dimension(390, 120);
				}

				@Override
				public Dimension getMinimumSize() {
					return new Dimension(390, 120);
				}

				@Override
				public Dimension getMaximumSize() {
					return new Dimension(390, 120);
				}
			};
			scroller
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			scroller
					.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			this.showGalleries();

			GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			this.add(scroller, constraints);

			JPanel buttonPanel = new JPanel(new GridBagLayout());
			constraints.insets = new Insets(4, 2, 2, 2);
			constraints.gridy = 0;
			constraints.gridx = 0;

			JButton oldButton = new MyJButton("Gewählte Galerie");
			oldButton.addActionListener(new OldGalleryListener());
			buttonPanel.add(oldButton, constraints);
			locationsBox = new MyJComboBox();
			locationsBox.setLocationsFile(Helper.getInstance().getFileLocation(
					"locations.list"));
			constraints.gridy = 0;
			constraints.gridx = 1;
			buttonPanel.add(locationsBox, constraints);
			constraints.gridy = 0;
			constraints.gridx = 2;
			JButton newButton = new MyJButton("Neue Galerie");
			newButton.addActionListener(new NewGalleryListener());
			buttonPanel.add(newButton, constraints);

			constraints.gridx = 0;
			constraints.gridy = 1;
			this.add(buttonPanel, constraints);

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			this.setLocation((d.width - getSize().width) / 2,
					(d.height - getSize().height) / 2);
			this.setVisible(true);
		}

		private void showGalleries() {
			this.galTableModel.setRowCount(0);
			for (Gallery gal : this.myGalleries) {
				this.galTableModel.addRow(new Object[] { gal, gal.getTitle(),
						new Integer(gal.getPictures()),
						new Boolean(gal.isIntern()) });
			}
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
				if (GalleryDialog.this.locationsBox.getSelectedItem() instanceof String
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

		private class OldGalleryListener extends MouseAdapter implements
				ActionListener {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2)
					this.selectOld();
			}

			public void actionPerformed(ActionEvent e) {
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
								(Gallery) GalleryDialog.this.galTable
										.getValueAt(GalleryDialog.this.galTable
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
	}
}
