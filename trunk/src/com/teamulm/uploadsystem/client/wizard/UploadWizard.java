package com.teamulm.uploadsystem.client.wizard;

import java.awt.Color;
import java.io.File;

import javax.imageio.ImageIO;

import org.pietschy.wizard.DefaultTitleComponent;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.WizardEvent;
import org.pietschy.wizard.WizardListener;
import org.pietschy.wizard.WizardModel;
import org.pietschy.wizard.models.Condition;

public class UploadWizard {

	private static final long serialVersionUID = -5298777332860158639L;

	private UploadWizardModel uploadWizardModel;

	private Wizard myWizard;

	public UploadWizard() {
		this.uploadWizardModel = new UploadWizardModel();
		this.uploadWizardModel.add(new LoginStep());
		this.uploadWizardModel.add(new SelectDateStep());
		this.uploadWizardModel.add(new GallerySelectStep());
		this.uploadWizardModel.add(new TitleDescriptionStep(), new Condition() {
			public boolean evaluate(WizardModel model) {
				if (model instanceof UploadWizardModel) {
					UploadWizardModel uploadWizardModel = (UploadWizardModel) model;
					return uploadWizardModel.getGallery() != null
							&& uploadWizardModel.getGallery().isNewGallery();
				}
				return false;
			}
		});
		this.uploadWizardModel.add(new PictureSelectStep());
		this.myWizard = new Wizard(this.uploadWizardModel);
		this.myWizard.setDefaultExitMode(Wizard.EXIT_ON_CLOSE);
		this.myWizard.addWizardListener(new WizardListener() {
			public void wizardCancelled(WizardEvent e) {
				UploadWizard.this.uploadWizardModel.abort();
			}

			public void wizardClosed(WizardEvent e) {
				UploadWizard.this.uploadWizardModel.abort();
			}
		});
		((DefaultTitleComponent) this.myWizard.getTitleComponent())
				.setFadeColor(Color.BLUE);
		((DefaultTitleComponent) this.myWizard.getTitleComponent())
				.setGradientBackground(true);
	}

	public void show() {
		try {
			this.myWizard.showInFrame("Team-Ulm Fotoupload", ImageIO
					.read(new File("misc/icon.gif")));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
