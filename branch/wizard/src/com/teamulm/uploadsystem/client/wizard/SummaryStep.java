package com.teamulm.uploadsystem.client.wizard;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

import com.teamulm.uploadsystem.client.layout.comp.MyJProgressBar;
import com.teamulm.uploadsystem.client.layout.comp.StatusList;

public class SummaryStep extends AbstractWizardStep {

	private UploadWizardModel model;

	private JPanel summaryView, progressView;

	private MyJProgressBar uploadProg, convertProg;

	private StatusList statusList;

	public SummaryStep() {
		super("Zusammenfassung",
				"Hier kannst du deine Angaben nochmal überprüfen, und den Upload starten.");
		this.setComplete(true);
		this.initProgressView();
	}

	private void initSummaryView() {
		this.summaryView = new JPanel();
		this.summaryView.add(new JLabel("Location:"));
		this.summaryView.add(new JLabel(this.model.getGallery().getLocation()));
		this.summaryView.add(new JLabel("Datum:"));
		this.summaryView.add(new JLabel(this.model.getGallery().getDate()
				.replace('-', '.')));
		this.summaryView.add(new JLabel("Vorhandene Bilder:"));
		this.summaryView.add(new JLabel(new Integer(this.model.getGallery()
				.getPictures()).toString()));
		this.summaryView.add(new JLabel("Intern:"));
		this.summaryView.add(new JLabel(new Boolean(this.model.getGallery()
				.isIntern()).toString()));
		this.summaryView.add(new JLabel("Titel:"));
		this.summaryView.add(new JLabel(this.model.getGallery().getTitle()));
		this.summaryView.add(new JLabel("Beschreibung:"));
		this.summaryView.add(new JLabel(this.model.getGallery().getDesc()));
	}

	private void initProgressView() {
		this.progressView = new JPanel(new FlowLayout());
		this.progressView.add(new JLabel("Upload:"));
		this.uploadProg = new MyJProgressBar();
		this.progressView.add(this.uploadProg);
		this.progressView.add(new JLabel("Konvertierung:"));
		this.convertProg = new MyJProgressBar();
		this.progressView.add(this.convertProg);
		this.statusList = new StatusList();
		this.progressView.add(this.statusList);
	}

	@Override
	public void init(WizardModel model) {
		this.model = (UploadWizardModel) model;
	}

	public void applyState() throws InvalidStateException {
		setBusy(true);
		setView(this.progressView);
		this.model.getTrmEngine().setProgressBars(this.uploadProg,
				this.convertProg);
		this.model.getTrmEngine().start();
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}

	public void prepare() {
		this.initSummaryView();
		setView(this.summaryView);
	}
}
