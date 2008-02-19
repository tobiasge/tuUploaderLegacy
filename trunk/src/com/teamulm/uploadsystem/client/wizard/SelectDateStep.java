package com.teamulm.uploadsystem.client.wizard;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

public class SelectDateStep extends AbstractWizardStep {

	private UploadWizardModel model;

	public SelectDateStep() {
		super("Datum wählen", "Das Datum der Galerie wählen.");
		this.setComplete(true);
	}

	@Override
	public void init(WizardModel model) {
		this.model = (UploadWizardModel) model;
	}

	public void applyState() throws InvalidStateException {
		this.model.setPossibleGalleries(this.model.getTrmEngine()
				.getGalleriesFor("09-02-2008"));
		this.model.setDate("09-02-2008");
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}

	public void prepare() {
		setView(new JPanel());
	}
}
