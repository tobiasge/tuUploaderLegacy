package com.teamulm.uploadsystem.client.wizard;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

import com.teamulm.uploadsystem.client.layout.comp.MyJComboBox;
import com.teamulm.uploadsystem.client.listener.al.ALUpdate;
import com.teamulm.uploadsystem.data.Gallery;

public class GallerySelectStep extends AbstractWizardStep {

	private UploadWizardModel model;

	private ArrayList<Gallery> possibleGalleries;

	private JPanel mainView;
	private JTable galTable;
	private MyJComboBox locationsBox;

	private DefaultTableModel galTableModel;
	private JRadioButton newGal, selGal;

	public GallerySelectStep() {
		super("Galerie wählen",
				"Hier eine Galerie auswählen, oder eine Neue erstellen.");
		this.possibleGalleries = new ArrayList<Gallery>();
		this.initMainView();
	}

	public void applyState() throws InvalidStateException {
		/*
		 * if (this.selGal.isSelected()) { this.model.setGallery((Gallery)
		 * this.galTable.getValueAt( this.galTable.getSelectedRow(), 0)); } else {
		 * Gallery gal = new Gallery(); gal.setLocation((String)
		 * this.locationsBox.getSelectedLoc());
		 * gal.setDate(this.model.getDate()); this.model.setGallery(gal); }
		 */
	}

	@Override
	public void init(WizardModel model) {
		this.model = (UploadWizardModel) model;
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}

	public void prepare() {
		this.possibleGalleries = this.model.getPossibleGalleries();
		this.showGalleries();
		this.setView(mainView);
	}

	private void initMainView() {
		this.mainView = new JPanel();
		newGal = new JRadioButton("Neue Galerie");
		newGal.addActionListener(new RadioButtonListener());
		newGal.setActionCommand("newGal");
		selGal = new JRadioButton("Alte Galerie");
		selGal.addActionListener(new RadioButtonListener());
		selGal.setActionCommand("selGal");
		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(newGal);
		bGroup.add(selGal);
		JPanel bPanel = new JPanel(new FlowLayout());
		bPanel.add(newGal);
		bPanel.add(selGal);
		this.mainView.add(bPanel);
		this.mainView.setLayout(new FlowLayout());
		String[] header = { "Location", "Bilder", "Intern" };
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
		this.galTable.getSelectionModel().addListSelectionListener(
				new GalleryListener());
		this.galTable.setModel(this.galTableModel);
		this.galTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.galTable.setColumnSelectionAllowed(false);
		this.galTable.setRowSelectionAllowed(true);
		this.galTable.getTableHeader().setReorderingAllowed(false);
		this.galTable.getTableHeader().setResizingAllowed(true);
		JScrollPane scroller = new JScrollPane(this.galTable) {
			private static final long serialVersionUID = 0L;

			@Override
			public Dimension getPreferredSize() {
				return new Dimension(350, 200);
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(350, 200);
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(350, 200);
			}
		};
		scroller
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scroller
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.mainView.add(scroller);
		this.mainView.add(new JLabel("Neue Galerie: "));
		this.locationsBox = new MyJComboBox();
		this.locationsBox.addActionListener(new GalleryListener());
		this.locationsBox.setLocationsFile("locations.list");
		this.mainView.add(this.locationsBox);
		JButton updateButton = new JButton("Locations laden");
		updateButton.addActionListener(new ALUpdate(this.locationsBox));
		this.mainView.add(updateButton);
	}

	private void showGalleries() {
		this.galTableModel.setRowCount(0);
		for (Gallery gal : this.possibleGalleries) {
			this.galTableModel
					.addRow(new Object[] { gal, new Integer(gal.getPictures()),
							new Boolean(gal.isIntern()) });
			this.galTable.updateUI();

		}
	}

	private class RadioButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equalsIgnoreCase("newGal")) {
				GallerySelectStep.this.galTable.setEnabled(false);
				GallerySelectStep.this.galTable.removeRowSelectionInterval(0,
						GallerySelectStep.this.galTable.getRowCount() - 1);
				if (((String) GallerySelectStep.this.locationsBox
						.getSelectedItem())
						.equalsIgnoreCase("    -- Bitte wählen --")) {
					GallerySelectStep.this.setComplete(false);
				}
				GallerySelectStep.this.locationsBox.setEnabled(true);
			} else if (e.getActionCommand().equalsIgnoreCase("selGal")) {
				GallerySelectStep.this.galTable.setEnabled(true);
				GallerySelectStep.this.locationsBox.setEnabled(false);
			}
		}
	}

	private class GalleryListener implements ListSelectionListener,
			ActionListener {

		public void valueChanged(ListSelectionEvent e) {
			if (e.getSource() instanceof DefaultListSelectionModel) {
				if (-1 == GallerySelectStep.this.galTable.getSelectedRow())
					return;
				GallerySelectStep.this.model
						.setGallery((Gallery) GallerySelectStep.this.galTable
								.getValueAt(GallerySelectStep.this.galTable
										.getSelectedRow(), 0));
				GallerySelectStep.this.setComplete(true);
				GallerySelectStep.this.selGal.doClick();
			}
		}

		public void actionPerformed(ActionEvent e) {
			if (e.getSource() instanceof MyJComboBox) {
				MyJComboBox locCombo = (MyJComboBox) e.getSource();
				if (!((String) locCombo.getSelectedItem())
						.equalsIgnoreCase("    -- Bitte wählen --")) {
					Gallery gal = new Gallery();
					gal
							.setLocation((String) GallerySelectStep.this.locationsBox
									.getSelectedLoc());
					gal.setDate(GallerySelectStep.this.model.getDate());
					GallerySelectStep.this.model.setGallery(gal);
					GallerySelectStep.this.setComplete(true);
					GallerySelectStep.this.newGal.doClick();
				}
			}

		}
	}
}
