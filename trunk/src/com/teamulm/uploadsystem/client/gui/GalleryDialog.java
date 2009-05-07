package com.teamulm.uploadsystem.client.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;
import com.teamulm.uploadsystem.data.Location;

public class GalleryDialog extends Dialog {

	private static final int TITLEMAXLENGTH = 33;

	private static final int DESCRMAXLENGTH = 180;

	private ArrayList<Gallery> myGalleries;

	private Combo locationsBox;

	private Text titleField, descField;

	private Table galTable;

	private String date;

	private Button newGal, oldGal, isIntern;

	private Composite newGalComposite;

	public GalleryDialog(Shell parentShell, String date) {
		super(parentShell);
		this.date = date;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.getShell().setText("Galerien am " + this.date.replace('-', '.'));
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.fillDefaults().margins(5, 5).applyTo(composite);
		SelectionListener oldNewGal = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				Button source = (Button) selectionEvent.widget;
				if (source.getSelection()) {
					if (GalleryDialog.this.newGal.equals(source)) {
						GalleryDialog.this.setNewGalEnabled(true);
					} else if (GalleryDialog.this.oldGal.equals(source)) {
						GalleryDialog.this.setNewGalEnabled(false);
					}
				}
			}
		};

		this.oldGal = new Button(composite, SWT.RADIO);
		this.oldGal.setText("Vorhandene Galerie wählen");
		this.oldGal.addSelectionListener(oldNewGal);
		GridDataFactory.fillDefaults().applyTo(this.oldGal);
		this.galTable = new Table(composite, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		this.galTable.setHeaderVisible(true);
		this.galTable.setLinesVisible(true);
		this.buildGalleryTable();
		this.galTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent mouseEvent) {
				if (-1 < GalleryDialog.this.galTable.getSelectionIndex()) {
					Gallery gallery = GalleryDialog.this.myGalleries.get(GalleryDialog.this.galTable
						.getSelectionIndex());
					if (null != gallery) {
						new ViewGalleryDialog(GalleryDialog.this.getShell(), gallery).open();
					}
				}
			}
		});
		new GalleryLoader(this.date).start();
		GridDataFactory.fillDefaults().hint(425, 120).applyTo(this.galTable);
		this.newGal = new Button(composite, SWT.RADIO);
		this.newGal.setText("Neue Galerie erstellen");
		this.newGal.addSelectionListener(oldNewGal);
		GridDataFactory.fillDefaults().applyTo(this.newGal);
		this.buildNewGalComposite(composite);
		GridDataFactory.fillDefaults().applyTo(this.newGalComposite);
		this.setNewGalEnabled(false);
		return composite;
	}

	private void buildGalleryTable() {
		TableColumn col = null;
		int columnIndex = 0;

		col = new TableColumn(this.galTable, SWT.LEFT, columnIndex++);
		col.setText("Location");
		col.setWidth(130);
		col.setResizable(false);

		col = new TableColumn(this.galTable, SWT.LEFT, columnIndex++);
		col.setText("Titel");
		col.setWidth(200);
		col.setResizable(false);

		col = new TableColumn(this.galTable, SWT.LEFT, columnIndex++);
		col.setText("Bilder");
		col.setWidth(40);
		col.setResizable(false);

		col = new TableColumn(this.galTable, SWT.LEFT | SWT.CHECK, columnIndex++);
		col.setText("Intern");
		col.setWidth(50);
		col.setResizable(false);
	}

	private void buildNewGalComposite(Composite parent) {
		this.newGalComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(this.newGalComposite);
		this.locationsBox = new Combo(this.newGalComposite, SWT.BORDER | SWT.READ_ONLY);
		this.locationsBox.setVisibleItemCount(10);
		this.locationsBox.setItems(new String[] { "    -- Bitte wählen --" });
		this.locationsBox.setText("    -- Bitte wählen --");
		GridDataFactory.fillDefaults().applyTo(this.locationsBox);
		new LocationsLoader(this.locationsBox).start();
		this.titleField = new Text(this.newGalComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.titleField);
		this.isIntern = new Button(this.newGalComposite, SWT.CHECK);
		GridDataFactory.fillDefaults().hint(55, SWT.DEFAULT).indent(5, SWT.DEFAULT).applyTo(this.isIntern);
		this.descField = new Text(this.newGalComposite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(this.descField);
	}

	private void setNewGalEnabled(boolean enabled) {
		this.galTable.setEnabled(!enabled);
		this.titleField.setEnabled(enabled);
		this.descField.setEnabled(enabled);
		this.locationsBox.setEnabled(enabled);
		this.isIntern.setEnabled(enabled);
	}

	private class LocationsLoader extends Thread {

		private Combo locationsBox;

		public LocationsLoader(Combo locationsBox) {
			this.locationsBox = locationsBox;
			this.setDaemon(true);
		}

		@Override
		public void run() {
			List<Location> locations = TrmEngine.getInstance().getLocations();
			if (null == locations) {
				TeamUlmUpload.getInstance().getMainWindow().addStatusLine("Locationsliste nicht geladen");
				return;
			}
			final String[] locNames = new String[locations.size() + 1];
			Collections.sort(locations);
			int index = 0;
			locNames[index++] = "    -- Bitte wählen --";
			for (Location location : locations) {
				locNames[index++] = location.getName();
			}
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					if (LocationsLoader.this.locationsBox.isDisposed()) {
						return;
					}
					LocationsLoader.this.locationsBox.setItems(locNames);
					LocationsLoader.this.locationsBox.setText("    -- Bitte wählen --");
					TeamUlmUpload.getInstance().getMainWindow().addStatusLine("Locationsliste laden fertig");
				}
			});
		}
	}

	private class GalleryLoader extends Thread {

		private String date;

		public GalleryLoader(String date) {
			this.date = date;
			this.setDaemon(true);
		}

		@Override
		public void run() {
			final ArrayList<Gallery> list = TrmEngine.getInstance().getGalleriesFor(this.date);
			Collections.sort(list);
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					if (GalleryDialog.this.galTable.isDisposed()) {
						return;
					}
					GalleryDialog.this.myGalleries = list;
					for (Gallery gallery : list) {
						TableItem item = new TableItem(GalleryDialog.this.galTable, SWT.DEFAULT);
						item.setText(new String[] { gallery.getLocation(), gallery.getTitle(),
							"" + gallery.getPictures(), gallery.isIntern() ? "Ja" : "Nein" });
					}
					TeamUlmUpload.getInstance().getMainWindow().addStatusLine("Galerien laden fertig");
				}
			});
		}
	}
}
