package com.teamulm.uploadsystem.client.wizard;

import java.awt.Color;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.pietschy.wizard.I18n;
import org.pietschy.wizard.OverviewProvider;
import org.pietschy.wizard.WizardStep;
import org.pietschy.wizard.models.DynamicModel;

import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class UploadWizardModel extends DynamicModel implements OverviewProvider {

	private TrmEngine trmEngine;

	private DynamicModelOverview overviewComponent;

	private Gallery galleryToUpload;

	private ArrayList<Gallery> possibleGalleries;

	private String date;

	protected String getDate() {
		return date;
	}

	protected void setDate(String date) {
		this.date = date;
	}

	public UploadWizardModel() {
		super();
		this.trmEngine = new TrmEngine();
	}

	public boolean isLastAvailable() {
		return false;
	}

	public boolean isLastVisible() {
		return false;
	}

	protected TrmEngine getTrmEngine() {
		return this.trmEngine;
	}

	public JComponent getOverviewComponent() {
		if (overviewComponent == null)
			overviewComponent = new DynamicModelOverview(this);
		return overviewComponent;
	}

	protected Gallery getGallery() {
		return this.galleryToUpload;
	}

	protected void setGallery(Gallery newGallery) {
		this.galleryToUpload = newGallery;
		if (null != this.trmEngine) {
			this.trmEngine.setGallery(newGallery);
		}
	}

	private class DynamicModelOverview extends JPanel implements
			OverviewProvider, PropertyChangeListener {

		private static final long serialVersionUID = 1717931188145750993L;
		private HashMap<WizardStep, JLabel> labels = new HashMap<WizardStep, JLabel>();

		private UploadWizardModel model;

		protected DynamicModelOverview(UploadWizardModel model) {
			this.model = model;
			this.model.addPropertyChangeListener(this);
			setBackground(Color.WHITE);
			setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

			JLabel title = new JLabel(I18n
					.getString("StaticModelOverview.title"));
			title.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
			title.setAlignmentX(0);
			title.setMaximumSize(new Dimension(Integer.MAX_VALUE, title
					.getMaximumSize().height));
			add(title);
			int i = 1;
			for (Iterator<?> iter = model.stepIterator(); iter.hasNext();) {
				WizardStep step = (WizardStep) iter.next();
				JLabel label = new JLabel("" + i++ + ". " + step.getName());
				label.setBackground(new Color(240, 240, 240));
				label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
				label.setAlignmentX(0);
				label.setMaximumSize(new Dimension(Integer.MAX_VALUE, label
						.getMaximumSize().height));
				add(label);
				labels.put(step, label);
			}

			add(Box.createGlue());
		}

		public JComponent getOverviewComponent() {
			return this;
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if (evt.getPropertyName().equals("activeStep")) {
				JLabel old = (JLabel) labels.get(evt.getOldValue());
				if (old != null)
					formatInactive(old);

				JLabel label = (JLabel) labels.get(evt.getNewValue());
				formatActive(label);
				repaint();
			}
		}

		protected void formatActive(JLabel label) {
			label.setOpaque(true);
		}

		protected void formatInactive(JLabel label) {
			label.setOpaque(false);
		}

	}

	protected ArrayList<Gallery> getPossibleGalleries() {
		return possibleGalleries;
	}

	protected void setPossibleGalleries(ArrayList<Gallery> possibleGalleries) {
		this.possibleGalleries = possibleGalleries;
	}

	protected void abort() {
		this.trmEngine.disconnect();
	}
}
