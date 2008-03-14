package com.teamulm.uploadsystem.client.layout;

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.layout.comp.FileList;
import com.teamulm.uploadsystem.client.layout.comp.MyDateEditor;
import com.teamulm.uploadsystem.client.layout.comp.MyJButton;
import com.teamulm.uploadsystem.client.layout.comp.MyJComboBox;
import com.teamulm.uploadsystem.client.layout.comp.MyJProgressBar;
import com.teamulm.uploadsystem.client.layout.comp.MyJTextField;
import com.teamulm.uploadsystem.client.layout.comp.StatusList;
import com.teamulm.uploadsystem.client.listener.al.ALChoosePic;
import com.teamulm.uploadsystem.client.listener.al.ALConAUpl;
import com.teamulm.uploadsystem.client.listener.al.ALRemovePic;
import com.teamulm.uploadsystem.client.listener.al.ALUpdate;
import com.teamulm.uploadsystem.client.listener.kl.KLEventTitle;
import com.teamulm.uploadsystem.client.listener.wl.WLMainClose;

@SuppressWarnings("serial")
public class MainWindow extends JFrame {

	private static final int TITLEMAXLENGTH = 33;

	private static final int DESCRMAXLENGTH = 180;

	private static MainWindow instance;

	private FileList fileList;

	private MyJTextField fieldTitle, fieldDesc;

	private MyJComboBox comboLocations;

	private MyJProgressBar uploadProgress, convertProgress;

	private StatusList statusList;

	private JCheckBox intern;

	private MyDateEditor eventDate;

	private JLabel selectedPics;

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
				return new Dimension(300, 200);
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(300, 200);
			}

			@Override
			public Dimension getMaximumSize() {
				return new Dimension(300, 200);
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
		infoPanel.add(new JLabel("Eventlocation:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 1;
		panelConstraints.insets = leftInsets;
		this.comboLocations = new MyJComboBox();
		infoPanel.add(comboLocations, panelConstraints);
		
		
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 2;
		panelConstraints.insets = leftInsets;
		MyJButton updateLocs = new MyJButton("Locations Update");
		updateLocs.addActionListener(new ALUpdate());
		infoPanel.add(updateLocs, panelConstraints);
		
		
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 3;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventdatum:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 3;
		panelConstraints.insets = leftInsets;
		this.eventDate = new MyDateEditor();
		infoPanel.add(this.eventDate, panelConstraints);
		
		
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 4;
		panelConstraints.insets = leftInsets;
		infoPanel.add(new MyJButton("Galerien laden"), panelConstraints);
		
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 5;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventtitel:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 5;
		panelConstraints.insets = leftInsets;
		this.fieldTitle = new MyJTextField(MainWindow.TITLEMAXLENGTH);
		this.fieldTitle.addKeyListener(new KLEventTitle());
		infoPanel.add(fieldTitle, panelConstraints);
		
		
		panelConstraints.gridx = 0;
		panelConstraints.gridy = 6;
		panelConstraints.insets = rightInsets;
		infoPanel.add(new JLabel("Eventbeschreibung:"), panelConstraints);
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 6;
		panelConstraints.insets = leftInsets;
		this.fieldDesc = new MyJTextField(MainWindow.DESCRMAXLENGTH);
		infoPanel.add(this.fieldDesc, panelConstraints);
		
	
		panelConstraints.gridx = 1;
		panelConstraints.gridy = 7;
		panelConstraints.insets = leftInsets;
		this.intern = new JCheckBox("Intern", false);
		infoPanel.add(this.intern, panelConstraints);
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
				TeamUlmUpload.getInstance().engineKill();
				MainWindow.this.reset();
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

	public String getEventTitle() {
		return this.fieldTitle.getText();
	}

	public String getEventDesc() {
		return this.fieldDesc.getText();
	}

	public String getEventLocation() {
		return this.comboLocations.getSelectedLoc();
	}

	public void addStatusLine(String line) {
		this.statusList.addStatusLine(line);
	}

	public MyDateEditor getDateEditor() {
		return this.eventDate;
	}

	public MyJComboBox getLocations() {
		return this.comboLocations;
	}

	public boolean getIntern() {
		return this.intern.isSelected();
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
		this.intern.setSelected(false);
		this.comboLocations.setSelectedIndex(0);
	}

	public void populateFields() {
		this.comboLocations.setLocationsFile("locations.list");
	}
}
