package com.teamulm.uploadsystem.client.gui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
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

	private static final String PLEASE_CHOOSE = Messages.getString("GalleryDialog.constant.pleaseSelect");

	public static final int DESCRMAXLENGTH = 180;

	public static final int TITLEMAXLENGTH = 33;

	private Date date;

	private Table galTable;

	private Combo locationsBox;

	private ArrayList<Gallery> myGalleries;

	private Button newGal, oldGal, isIntern;

	private Composite newGalComposite;

	private Text titleField, descField;

	public GalleryDialog(Shell parentShell, Date date) {
		super(parentShell);
		this.date = date;
	}

	private void buildGalleryTable() {
		TableColumn col = null;
		int columnIndex = 0;

		col = new TableColumn(this.galTable, SWT.LEFT, columnIndex++);
		col.setText(Messages.getString("GalleryDialog.table.galTable.column.location"));
		col.setWidth(130);
		col.setResizable(false);

		col = new TableColumn(this.galTable, SWT.LEFT, columnIndex++);
		col.setText(Messages.getString("GalleryDialog.table.galTable.column.title"));
		col.setWidth(200);
		col.setResizable(false);

		col = new TableColumn(this.galTable, SWT.LEFT, columnIndex++);
		col.setText(Messages.getString("GalleryDialog.table.galTable.column.pictures"));
		col.setWidth(40);
		col.setResizable(false);

		col = new TableColumn(this.galTable, SWT.LEFT | SWT.CHECK, columnIndex++);
		col.setText(Messages.getString("GalleryDialog.table.galTable.column.internal"));
		col.setWidth(50);
		col.setResizable(false);
	}

	private void buildNewGalComposite(Composite parent) {
		this.newGalComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).applyTo(this.newGalComposite);
		this.locationsBox = new Combo(this.newGalComposite, SWT.BORDER | SWT.READ_ONLY);
		this.locationsBox.setVisibleItemCount(10);
		this.locationsBox.setItems(new String[] { GalleryDialog.PLEASE_CHOOSE });
		this.locationsBox.setText(GalleryDialog.PLEASE_CHOOSE);
		GridDataFactory.fillDefaults().applyTo(this.locationsBox);
		new LocationsLoader(this.locationsBox).start();
		this.titleField = new Text(this.newGalComposite, SWT.BORDER | SWT.SEARCH);
		this.titleField.setMessage(Messages.getString("GalleryDialog.text.galleryTitle.messages"));
		this.titleField.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent verifyEvent) {
				String text = ((Text) verifyEvent.widget).getText();
				if (GalleryDialog.TITLEMAXLENGTH < text.length()) {
					if (verifyEvent.character != SWT.BS && verifyEvent.character != SWT.DEL) {
						verifyEvent.doit = false;
					}
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(this.titleField);
		this.isIntern = new Button(this.newGalComposite, SWT.CHECK);
		this.isIntern.setText(Messages.getString("GalleryDialog.button.interal.text"));
		this.isIntern.setToolTipText(Messages.getString("GalleryDialog.button.interal.tooltip"));
		GridDataFactory.fillDefaults().hint(55, SWT.DEFAULT).indent(5, SWT.DEFAULT).applyTo(this.isIntern);
		this.descField = new Text(this.newGalComposite, SWT.BORDER | SWT.SEARCH);
		this.descField.setMessage(Messages.getString("GalleryDialog.text.galleryDesc.messages"));
		this.descField.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent verifyEvent) {
				String text = ((Text) verifyEvent.widget).getText();
				if (GalleryDialog.DESCRMAXLENGTH < text.length()) {
					if (verifyEvent.character != SWT.BS && verifyEvent.character != SWT.DEL) {
						verifyEvent.doit = false;
					}
				}
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(this.descField);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.getShell().setText(MessageFormat.format(Messages.getString("GalleryDialog.dialog.title"), this.date));
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
		this.oldGal.setText(Messages.getString("GalleryDialog.button.oldGal.text"));
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
		new GalleryLoader(Gallery.GALLERY_DATE_FORMAT.format(this.date)).start();
		GridDataFactory.fillDefaults().hint(425, 120).applyTo(this.galTable);
		this.newGal = new Button(composite, SWT.RADIO);
		this.newGal.setText(Messages.getString("GalleryDialog.button.newGal.text"));
		this.newGal.addSelectionListener(oldNewGal);
		GridDataFactory.fillDefaults().applyTo(this.newGal);
		this.buildNewGalComposite(composite);
		GridDataFactory.fillDefaults().applyTo(this.newGalComposite);
		this.setNewGalEnabled(false);
		return composite;
	}

	@Override
	protected void okPressed() {
		if (this.oldGal.getSelection()) {
			if (-1 == this.galTable.getSelectionIndex()) {
				MessageBox mb = new MessageBox(this.getShell(), SWT.OK | SWT.ERROR);
				mb.setMessage(Messages.getString("GalleryDialog.msg.error.noGallery"));
				mb.setText(Messages.getString("String.error"));
				mb.open();
				return;
			} else {
				Gallery gal = (Gallery) this.galTable.getItem(this.galTable.getSelectionIndex()).getData();
				TeamUlmUpload.getInstance().getMainWindow().setGallery(gal);
			}
		} else if (this.newGal.getSelection()) {
			if (0 < GalleryDialog.this.titleField.getText().length()
				&& 0 < GalleryDialog.this.descField.getText().length()
				&& !GalleryDialog.PLEASE_CHOOSE.equals(GalleryDialog.this.locationsBox.getText())) {
				Gallery gal = TrmEngine.getInstance().newGallery(GalleryDialog.this.locationsBox.getText(),
					Gallery.GALLERY_DATE_FORMAT.format(GalleryDialog.this.date));
				gal.setDesc(GalleryDialog.this.descField.getText());
				gal.setIntern(GalleryDialog.this.isIntern.getSelection());
				gal.setTitle(GalleryDialog.this.titleField.getText());
				TeamUlmUpload.getInstance().getMainWindow().setGallery(gal);
			} else {
				MessageBox mb = new MessageBox(this.getShell(), SWT.OK | SWT.ERROR);
				mb.setMessage(Messages.getString("GalleryDialog.msg.error.allFields"));
				mb.setText(Messages.getString("String.error"));
				mb.open();
				return;
			}
		} else {
			return;
		}
		super.okPressed();
	}

	private void setNewGalEnabled(boolean enabled) {
		this.galTable.setEnabled(!enabled);
		this.titleField.setEnabled(enabled);
		this.descField.setEnabled(enabled);
		this.locationsBox.setEnabled(enabled);
		this.isIntern.setEnabled(enabled);
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
							"" + gallery.getPictures(),
							gallery.isIntern() ? Messages.getString("String.yes") : Messages.getString("String.no") });
						item.setData(gallery);
					}
					TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
						Messages.getString("GalleryDialog.logMessages.galleriesLoaded"));
				}
			});
		}
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
				TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
					Messages.getString("GalleryDialog.logMessages.locationsNotLoaded"));
				return;
			}
			final String[] locNames = new String[locations.size() + 1];
			Collections.sort(locations);
			int index = 0;
			locNames[index++] = GalleryDialog.PLEASE_CHOOSE;
			for (Location location : locations) {
				locNames[index++] = location.getName();
			}
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					if (LocationsLoader.this.locationsBox.isDisposed()) {
						return;
					}
					LocationsLoader.this.locationsBox.setItems(locNames);
					LocationsLoader.this.locationsBox.setText(GalleryDialog.PLEASE_CHOOSE);
					TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
						Messages.getString("GalleryDialog.logMessages.locationsLoaded"));
				}
			});
		}
	}
}
