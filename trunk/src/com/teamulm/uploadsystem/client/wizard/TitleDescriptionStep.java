package com.teamulm.uploadsystem.client.wizard;

import java.awt.Dimension;

import javax.swing.JPanel;

import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

public class TitleDescriptionStep extends AbstractWizardStep {

	private UploadWizardModel model;

	public TitleDescriptionStep() {
		super(
				"Titel und Beschreibung",
				"Bitte einen Titel und eine Beschreibung für die neu erstellte Galerie eingeben.");
	}

	public void applyState() throws InvalidStateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(WizardModel model) {
		this.model = (UploadWizardModel) model;
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}

	public void prepare() {
		setView(new JPanel());
	}
}