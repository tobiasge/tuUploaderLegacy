package com.teamulm.uploadsystem.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.teamulm.uploadsystem.client.gui.comp.MyJButton;
import com.teamulm.uploadsystem.client.gui.comp.MyJComboBox;
import com.teamulm.uploadsystem.client.gui.comp.MyJTextField;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.Location;

public class GalleryDialog extends JDialog {

	private static final long serialVersionUID = -9193707556220629559L;

	private ArrayList<Gallery> myGalleries;
	private DefaultTableModel galTableModel;
	private MyJComboBox locationsBox;
	private MyJTextField titleField, descField;
	private JTable galTable;
	private String date;
	private JCheckBox isIntern;
	private JRadioButton newGal, oldGal;

	public GalleryDialog(String date) {
		super(MainWindow.getInstance(), "Galerien am " + date.replace('-', '.'), true);
		this.date = date;
		this.setPreferredSize(new Dimension(454, 290));
		this.setMinimumSize(new Dimension(454, 290));
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
		// this.galTable.getColumnModel().setColumnMargin(7);

		this.galTable.setDefaultRenderer(Object.class, new MyRenderer());

		this.setColumnWidth(0, 130);
		this.setColumnWidth(1, 200);
		this.setColumnWidth(2, 40);
		this.setColumnWidth(3, 40);

		JScrollPane scroller = new JScrollPane(this.galTable);
		scroller.setPreferredSize(new Dimension(428, 120));
		scroller.setMinimumSize(new Dimension(428, 120));
		scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		GridBagConstraints constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.LINE_START;
		ButtonGroup tmp = new ButtonGroup();
		this.oldGal = new JRadioButton("Vorhandene Galerie wählen");
		this.oldGal.addActionListener(new OldOrNewGalleryListener());
		this.oldGal.setSelected(true);

		this.newGal = new JRadioButton("Neue Galerie erstellen");
		this.newGal.addActionListener(new OldOrNewGalleryListener());

		tmp.add(this.oldGal);
		tmp.add(this.newGal);

		constraints.insets = new Insets(4, 2, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 0;
		this.add(this.oldGal, constraints);

		constraints.insets = new Insets(4, 6, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 1;
		this.add(scroller, constraints);
		new GalleryLoader(this.date, this).execute();

		constraints.insets = new Insets(4, 2, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 2;
		this.add(this.newGal, constraints);

		constraints.insets = new Insets(2, 6, 0, 2);
		constraints.gridx = 0;
		constraints.gridy = 3;
		this.add(this.buildNewGalPanel(), constraints);

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width - getSize().width) / 2, (d.height - getSize().height) / 2);

		this.switchFields(false);
		this.setVisible(true);
	}

	public MyJComboBox getLocationsBox() {
		return this.locationsBox;
	}

	private void showGalleries() {
		this.galTableModel.setRowCount(0);
		for (Gallery gal : this.myGalleries) {
			String tmp = "";
			if (gal.isIntern())
				tmp = "ja";
			else
				tmp = "nein";
			this.galTableModel.addRow(new Object[] { gal, gal.getTitle(), new Integer(gal.getPictures()), tmp });
		}
	}

	public void setGalleries(ArrayList<Gallery> galList) {
		this.myGalleries = galList;
	}

	private void switchFields(boolean toNewGal) {
		if (toNewGal) {
			this.galTable.clearSelection();
			this.galTable.setEnabled(false);

			this.descField.setEnabled(true);
			this.titleField.setEnabled(true);
			this.isIntern.setEnabled(true);
			this.locationsBox.setEnabled(true);
		} else {
			this.galTable.setEnabled(true);

			this.descField.setEnabled(false);
			this.titleField.setEnabled(false);
			this.isIntern.setEnabled(false);
			this.locationsBox.setEnabled(false);
		}
	}

	private void setColumnWidth(int column, int width) {
		this.galTable.getColumnModel().getColumn(column).setPreferredWidth(width);
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
		cons.insets = new Insets(0, 0, 0, 50);
		this.titleField = new MyJTextField(100);
		this.titleField.setPreferredSize(new Dimension(200, 20));
		this.titleField.setMinimumSize(new Dimension(200, 20));
		this.titleField.setForeground(Color.GRAY);
		this.titleField.setText("Titel");
		this.titleField.addFocusListener(new TextFieldFocus("Titel"));
		line1Panel.add(this.titleField, cons);

		cons.gridx = 2;
		cons.gridy = 0;
		cons.anchor = GridBagConstraints.LINE_END;
		cons.insets = new Insets(0, 0, 0, 36);
		this.isIntern = new JCheckBox();
		line1Panel.add(this.isIntern, cons);

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
		JButton OKButton = new MyJButton("Übernehmen");
		OKButton.addActionListener(new OkButtonListener());
		newPanel.add(OKButton, cons);

		return newPanel;
	}

	private void selectOldGal() {
		if (-1 == this.galTable.getSelectedRow()) {
			JOptionPane.showMessageDialog(GalleryDialog.this, "Bitte eine Galerie auswählen!", "Galerie...",
				JOptionPane.ERROR_MESSAGE, null);
		} else {
			if (GalleryDialog.this.galTable.getValueAt(GalleryDialog.this.galTable.getSelectedRow(), 0) instanceof Gallery) {
				MainWindow.getInstance().setGallery(
					(Gallery) GalleryDialog.this.galTable.getValueAt(GalleryDialog.this.galTable.getSelectedRow(), 0));
				GalleryDialog.this.dispose();
			} else {
				JOptionPane.showMessageDialog(GalleryDialog.this, "Bitte eine Galerie auswählen!", "Galerie...",
					JOptionPane.ERROR_MESSAGE, null);
			}
		}
	}

	private void selectNewGal() {
		if (!(this.getLocationsBox().getSelectedItem() instanceof String)) {
			return;
		}
		if (((String) this.locationsBox.getSelectedItem()).compareTo("    -- Bitte wählen --") == 0
			|| this.descField.getText().equalsIgnoreCase("Beschreibung")
			|| this.titleField.getText().equalsIgnoreCase("Titel")) {
			JOptionPane.showMessageDialog(this, "Bitte alle Felder vollständig ausfüllen!", "Neue Galerie",
				JOptionPane.ERROR_MESSAGE, null);
		} else {
			Gallery tmpGal = TrmEngine.getInstance().newGallery((String) this.locationsBox.getSelectedItem(), date);
			tmpGal.setIntern(this.isIntern.isSelected());
			tmpGal.setDesc(this.descField.getText());
			tmpGal.setTitle(this.titleField.getText());
			MainWindow.getInstance().setGallery(tmpGal);
			GalleryDialog.this.dispose();
		}

	}

	private class OldOrNewGalleryListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (GalleryDialog.this.newGal.isSelected()) {
				GalleryDialog.this.switchFields(true);
			} else if (GalleryDialog.this.oldGal.isSelected()) {
				GalleryDialog.this.switchFields(false);
			}
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

	private class OkButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			if (GalleryDialog.this.newGal.isSelected()) {
				GalleryDialog.this.selectNewGal();
			} else if (GalleryDialog.this.oldGal.isSelected()) {
				GalleryDialog.this.selectOldGal();
			}

		}
	}

	private class LocationsLoader extends SwingWorker<List<Location>, Void> {

		private MyJComboBox locationsBox;

		public LocationsLoader(MyJComboBox locationsBox) {
			this.locationsBox = locationsBox;
		}

		@Override
		protected List<Location> doInBackground() throws Exception {
			return TrmEngine.getInstance().getLocations();
		}

		@Override
		protected void done() {
			try {
				List<Location> locations = this.get();
				if (null == locations) {
					MainWindow.getInstance().addStatusLine("Locationsliste nicht geladen");
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
			ArrayList<Gallery> galleryList = TrmEngine.getInstance().getGalleriesFor(this.date);
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
			if (field.getText().equalsIgnoreCase("") || field.getText().equalsIgnoreCase(this.defaultString)) {
				field.setForeground(Color.GRAY);
				field.setText(this.defaultString);
			}
		}
	}

	static class MyRenderer extends DefaultTableCellRenderer {

		private static final long serialVersionUID = -2545639336835517795L;

		private Border CACHED_SEL_BORDER = BorderFactory.createMatteBorder(0, 4, 0, 4, UIManager
			.getColor("Table.selectionBackground"));

		private Border CACHED_BORDER = BorderFactory.createMatteBorder(0, 4, 0, 4, UIManager
			.getColor("Table.background"));

		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
			boolean hasFocus, int row, int column) {
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			Border currentBorder = getBorder();
			if (isSelected) {
				((JComponent) this).setBorder(BorderFactory.createCompoundBorder(currentBorder, CACHED_SEL_BORDER));
			} else {
				((JComponent) this).setBorder(BorderFactory.createCompoundBorder(currentBorder, CACHED_BORDER));
			}
			return this;
		}
	}

}
