package com.teamulm.uploadsystem.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.teamulm.uploadsystem.client.gui.comp.FileList;
import com.teamulm.uploadsystem.client.gui.comp.MyDateEditor;
import com.teamulm.uploadsystem.client.gui.comp.MyJButton;
import com.teamulm.uploadsystem.client.gui.comp.MyJProgressBar;
import com.teamulm.uploadsystem.client.gui.comp.MyJTextField;
import com.teamulm.uploadsystem.client.gui.comp.StatusList;
import com.teamulm.uploadsystem.client.listener.al.ALChoosePic;
import com.teamulm.uploadsystem.client.listener.al.ALConAUpl;
import com.teamulm.uploadsystem.client.listener.al.ALGalleryLoad;
import com.teamulm.uploadsystem.client.listener.al.ALRemovePic;
import com.teamulm.uploadsystem.client.listener.kl.KLEventTitle;
import com.teamulm.uploadsystem.client.listener.wl.WLMainClose;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private static final int TITLEMAXLENGTH = 33;

	private static final int DESCRMAXLENGTH = 180;

	private static MainWindow instance;

	private FileList fileList;

	private MyJTextField fieldTitle, fieldDesc, fieldLocations;

	private MyJProgressBar uploadProgress, convertProgress;

	private StatusList statusList;

	private JCheckBox filedIntern;

	private MyDateEditor eventDate;

	private JLabel selectedPics;

	private Gallery gallery;

	private MainWindow() {
		this.setTitle("Team-Ulm Fotoupload");
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WLMainClose());
		this.setIconImage(new ImageIcon("icon.gif").getImage());
		this.setLayout(new GridLayout(1, 2));
		this.add(this.generateLeftPanel(new JPanel()));
		this.add(this.generateRightPanel(new JPanel()));
		this.pack();
		this.setResizable(false);
		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation((d.width - getSize().width) / 2,
				(d.height - getSize().height) / 2);
		this.setVisible(true);
	}

	private JPanel generateLeftPanel(JPanel panel) {
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3),
				new BevelBorder(BevelBorder.RAISED)));
		GridBagConstraints constraints = new GridBagConstraints();

		JPanel filePanel = new JPanel(new GridBagLayout());
		GridBagConstraints filePanelConstraints = new GridBagConstraints();
		filePanelConstraints.gridx = 0;
		filePanelConstraints.gridy = 0;
		filePanelConstraints.insets = new Insets(2, 2, 2, 2);
		filePanelConstraints.anchor = GridBagConstraints.WEST;
		this.selectedPics = new JLabel("Ausgewählte Bilder (0):");
		filePanel.add(this.selectedPics, filePanelConstraints);
		filePanelConstraints.gridy++;
		this.fileList = new FileList();
		JScrollPane scrollPane = new JScrollPane(this.fileList) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 210);
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(300, 210);
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(300, 210);
			}
		};
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		filePanel.add(scrollPane, filePanelConstraints);
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		constraints.insets = new Insets(2, 2, 2, 2);
		panel.add(filePanel, constraints);

		JPanel infoPanel = new JPanel(new GridBagLayout());
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.anchor = GridBagConstraints.NORTHWEST;
		panelConstraints.fill = GridBagConstraints.HORIZONTAL;
		Insets rightInsets = new Insets(2, 0, 2, 5);
		Insets leftInsets = new Insets(2, 5, 2, 0);

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 0;
		panelConstraints.insets = rightInsets;
		MyJButton buttonChoosePic = new MyJButton("Bilder wählen");
		buttonChoosePic.addActionListener(new ALChoosePic());
		infoPanel.add(buttonChoosePic, panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 0;
		panelConstraints.insets = leftInsets;
		MyJButton deleteSelection = new MyJButton("Auswahl löschen");
		deleteSelection.addActionListener(new ALRemovePic());
		infoPanel.add(deleteSelection, panelConstraints);

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 1;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventdatum:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 1;
		panelConstraints.insets = leftInsets;
		this.eventDate = new MyDateEditor();
		infoPanel.add(this.eventDate, panelConstraints);

		panelConstraints.gridx = 1;
		panelConstraints.gridy = 2;
		panelConstraints.insets = leftInsets;
		JButton galleryLoadButton = new MyJButton("Galerien wählen");
		galleryLoadButton.addActionListener(new ALGalleryLoad());
		infoPanel.add(galleryLoadButton, panelConstraints);

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 3;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventlocation:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 3;
		panelConstraints.insets = leftInsets;
		this.fieldLocations = new MyJTextField(0);
		this.fieldLocations.setEnabled(false);
		infoPanel.add(fieldLocations, panelConstraints);

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 4;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventtitel:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 4;
		panelConstraints.insets = leftInsets;
		this.fieldTitle = new MyJTextField(MainWindow.TITLEMAXLENGTH);
		this.fieldTitle.addKeyListener(new KLEventTitle());
		infoPanel.add(fieldTitle, panelConstraints);

		panelConstraints.gridx = 0;
		panelConstraints.gridy = 5;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventbeschreibung:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 5;
		panelConstraints.insets = leftInsets;
		this.fieldDesc = new MyJTextField(MainWindow.DESCRMAXLENGTH);
		infoPanel.add(this.fieldDesc, panelConstraints);

		panelConstraints.gridx = 1;
		panelConstraints.gridy = 6;
		panelConstraints.insets = leftInsets;
		this.filedIntern = new JCheckBox("Intern", false);
		infoPanel.add(this.filedIntern, panelConstraints);
		constraints.gridy++;
		panel.add(infoPanel, constraints);
		return panel;
	}

	private JPanel generateRightPanel(JPanel panel) {
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3),
				new BevelBorder(BevelBorder.RAISED)));
		GridBagConstraints panelConstraints = new GridBagConstraints();
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 0;
		panelConstraints.insets = new Insets(2, 2, 2, 2);
		panelConstraints.anchor = GridBagConstraints.WEST;
		panel.add(new JLabel("Konvertier-Fortschritt:"), panelConstraints);
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 1;
		this.convertProgress = new MyJProgressBar();
		panel.add(this.convertProgress, panelConstraints);
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 2;
		panel.add(new JLabel("Upload-Fortschritt:"), panelConstraints);
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 3;
		this.uploadProgress = new MyJProgressBar();
		panel.add(this.uploadProgress, panelConstraints);
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 4;
		this.statusList = new StatusList();
		JScrollPane scrollPane = new JScrollPane(this.statusList) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(300, 290);
			}
		};
		scrollPane.setWheelScrollingEnabled(true);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, panelConstraints);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		JButton convertButton = new JButton(
				"<html><body><center>Konvertieren <br>&amp; Hochladen</center></body></html>");
		convertButton.addActionListener(new ALConAUpl());
		buttonPanel.add(convertButton, BorderLayout.WEST);
		JButton resetButton = new JButton("Zurücksetzen");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
						MainWindow.getInstance(), "Wirklich zurücksetzten?",
						"Reset?", JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE)) {
					MainWindow.this.reset();
					TrmEngine.kill();
					System.gc();
				}
			}
		});
		buttonPanel.add(resetButton, BorderLayout.EAST);
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 5;
		panelConstraints.anchor = GridBagConstraints.CENTER;
		panelConstraints.fill = GridBagConstraints.HORIZONTAL;
		panel.add(buttonPanel, panelConstraints);
		return panel;
	}

	public static MainWindow getInstance() {
		if (null == MainWindow.instance) {
			MainWindow.instance = new MainWindow();
		}
		return MainWindow.instance;
	}

	public FileList getFileList() {
		return this.fileList;
	}

	public void addStatusLine(String line) {
		this.statusList.addStatusLine(line);
	}

	public MyDateEditor getDateEditor() {
		return this.eventDate;
	}

	public void setConvertProgress(int progress) {
		this.convertProgress.setProgress(progress);
	}

	public void setUploadProgress(int progress) {
		this.uploadProgress.setProgress(progress);
	}

	public void setFocus(String component) {
		if ("eventDesc".equalsIgnoreCase(component)) {
			this.fieldDesc.requestFocus();
		}
	}

	public void setSelectedPicText(String text) {
		this.selectedPics.setText(text);
		this.selectedPics.revalidate();
	}

	private void reset() {
		this.eventDate.setDateToday();
		this.fieldDesc.setText("");
		this.fieldTitle.setText("");
		this.convertProgress.reset();
		this.uploadProgress.reset();
		this.fileList.clearAllFiles();
		this.filedIntern.setSelected(false);
		this.fieldLocations.setText("");
	}

	public Gallery getGallery() {
		if (this.gallery.isNewGallery()) {
			this.gallery.setTitle(this.fieldTitle.getText());
			this.gallery.setDesc(this.fieldDesc.getText());
			this.gallery.setIntern(this.filedIntern.isSelected());
		}
		return this.gallery;
	}

	public void setGallery(Gallery gallery) {
		if (gallery.isNewGallery()) {
			this.fieldLocations.setText(gallery.getLocation());
			this.filedIntern.setSelected(false);
			this.fieldTitle.setEnabled(true);
			this.fieldDesc.setEnabled(true);
			this.filedIntern.setEnabled(true);
			this.fieldTitle.requestFocus();
		} else {
			this.fieldLocations.setText(gallery.getLocation());
			this.fieldTitle.setText(gallery.getTitle());
			this.fieldDesc.setText(gallery.getDesc());
			this.filedIntern.setSelected(gallery.isIntern());
			this.fieldTitle.setEnabled(false);
			this.fieldDesc.setEnabled(false);
			this.filedIntern.setEnabled(false);
		}
		this.gallery = gallery;
	}
}
