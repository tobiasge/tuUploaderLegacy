package com.teamulm.uploadsystem.client.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

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
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

@SuppressWarnings("serial")
public class MainWindow extends Window {

	private static final int TITLEMAXLENGTH = 33;

	private static final int DESCRMAXLENGTH = 180;

	private FileList fileList;

	private MyJTextField fieldTitle, fieldDesc, fieldLocations;

	private MyJProgressBar uploadProgress, convertProgress;

	private StatusList statusList;

	private JCheckBox filedIntern;

	private MyDateEditor eventDate;

	private JLabel selectedPics;

	private Gallery gallery;

	public MainWindow() {
		super((Shell) null);
		int style = this.getShellStyle();
		style &= ~(SWT.MAX | SWT.MIN | SWT.RESIZE);
		this.setShellStyle(style);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(new Image(Display.getCurrent(), "misc/icon.png"));
		newShell.setText("Team-Ulm.de Fotoupload");
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = (Composite) super.createContents(parent);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).margins(5, 5).applyTo(composite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.leftComposite(composite));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.rightComposite(composite));
		return composite;
	}

	private Composite leftComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);
		Label selectedPics = new Label(composite, SWT.NONE);
		selectedPics.setText("Ausgewählte Bilder (0):");
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(selectedPics);

		List selectedPicList = new List(composite, SWT.BORDER | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).hint(300, 210).applyTo(selectedPicList);

		Button selectPics = new Button(composite, SWT.PUSH);
		selectPics.setText("Bilder wählen");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectPics);

		Button deletedPictures = new Button(composite, SWT.PUSH);
		deletedPictures.setText("Auswahl löschen");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(deletedPictures);

		Label labelEvenDate = new Label(composite, SWT.NONE);
		labelEvenDate.setText("Eventdatum");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEvenDate);

		CDateTime gallerDate = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(gallerDate);

		Label labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText("");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		Button selectGallery = new Button(composite, SWT.PUSH);
		selectGallery.setText("Galerie wählen");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectGallery);

		Label labelEventLocation = new Label(composite, SWT.NONE);
		labelEventLocation.setText("Eventlocation:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventLocation);

		Text textEventLocation = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(textEventLocation);

		Label labelEventTitle = new Label(composite, SWT.NONE);
		labelEventTitle.setText("EventTitel:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventTitle);

		Text textEventTitle = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(textEventTitle);

		Label labelEventDescription = new Label(composite, SWT.NONE);
		labelEventDescription.setText("EventBeschreibung:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventDescription);

		Text textEventDescription = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(120, 60).applyTo(textEventDescription);

		labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText("");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		Button buttonIntern = new Button(composite, SWT.CHECK);
		buttonIntern.setText("Intern");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(buttonIntern);
		return composite;
	}

	private Composite rightComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(composite);

		Label labelConvertProgress = new Label(composite, SWT.NONE);
		labelConvertProgress.setText("Konvertier-Fortschritt:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelConvertProgress);

		ProgressBar convertProgressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(convertProgressBar);

		Label labelUploadProgress = new Label(composite, SWT.NONE);
		labelUploadProgress.setText("Upload-Fortschritt:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelUploadProgress);

		ProgressBar uploadProgressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(uploadProgressBar);

		List statusList = new List(composite, SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(290, 340).applyTo(statusList);
		statusList.add("Copyright by ibTEC Team-Ulm GbR");
		for (int i = 0; i < 30; i++) {
			statusList.add("Eintrag " + i);

		}

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(buttonComposite);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3).applyTo(buttonComposite);

		Button buttonUpload = new Button(buttonComposite, SWT.PUSH);
		buttonUpload.setText("Konvertieren / hochladen");
		GridDataFactory.fillDefaults().span(1, 1).grab(false, false).applyTo(buttonUpload);

		Label tmpLabel = new Label(buttonComposite, SWT.NONE);
		tmpLabel.setText("  ");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(tmpLabel);

		Button buttonReset = new Button(buttonComposite, SWT.PUSH);
		buttonReset.setText("Zurücksetzten");
		GridDataFactory.fillDefaults().span(1, 1).grab(false, false).applyTo(buttonReset);

		return composite;
	}

	private JPanel generateRightPanel(JPanel panel) {
		panel.setLayout(new GridBagLayout());
		panel.setBorder(new CompoundBorder(new EmptyBorder(3, 3, 3, 3), new BevelBorder(BevelBorder.RAISED)));
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
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		panel.add(scrollPane, panelConstraints);

		JPanel buttonPanel = new JPanel(new BorderLayout());
		JButton convertButton = new JButton(
			"<html><body><center>Konvertieren <br>&amp; Hochladen</center></body></html>");
		convertButton.addActionListener(new ALConAUpl());
		buttonPanel.add(convertButton, BorderLayout.WEST);
		JButton resetButton = new JButton("Zurücksetzen");
		resetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(MainWindow.getInstance(),
					"Wirklich zurücksetzten?", "Reset?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE)) {
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
		this.fieldLocations.setText(gallery.getLocation());
		this.fieldTitle.setText(gallery.getTitle());
		this.fieldDesc.setText(gallery.getDesc());
		this.filedIntern.setSelected(gallery.isIntern());
		if (gallery.isNewGallery()) {
			this.fieldTitle.setEnabled(true);
			this.fieldDesc.setEnabled(true);
			this.filedIntern.setEnabled(true);
		} else {
			this.fieldTitle.setEnabled(false);
			this.fieldDesc.setEnabled(false);
			this.filedIntern.setEnabled(false);
		}
		this.gallery = gallery;
	}

}
