package com.teamulm.uploadsystem.client.gui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamulm.uploadsystem.client.gui.comp.MyJProgressBar;
import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class MainWindow extends Window {

	private List fileList = null, statusList = null;

	private Text fieldTitle = null, fieldDesc = null, fieldLocation = null;

	private MyJProgressBar uploadProgress, convertProgress;

	private Button fieldIntern = null;

	private CDateTime eventDate = null;

	private Label selectedPics = null;

	private Gallery gallery = null;

	public MainWindow() {
		super((Shell) null);
		int style = this.getShellStyle();
		style &= ~(SWT.MAX | SWT.RESIZE);
		this.setShellStyle(style);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setImage(new Image(Display.getCurrent(), "icons/icon.png"));
		newShell.setText(Messages.getString("mainWindow.dialog.title"));
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite composite = (Composite) super.createContents(parent);
		FillLayout fillLayout = new FillLayout(SWT.HORIZONTAL);
		fillLayout.marginHeight = 5;
		fillLayout.marginWidth = 5;
		fillLayout.spacing = 5;
		composite.setLayout(fillLayout);
		this.leftComposite(composite);
		this.rightComposite(composite);
		return composite;
	}

	private Composite leftComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(composite);
		this.selectedPics = new Label(composite, SWT.NONE);
		this.selectedPics.setText("Ausgewählte Bilder (0):");
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(this.selectedPics);

		this.fileList = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).hint(300, 210).applyTo(this.fileList);
		DropTarget fileListTarget = new DropTarget(this.fileList, DND.DROP_COPY);
		fileListTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		fileListTarget.addDropListener(new DropTargetListener() {

			@Override
			public void dropAccept(DropTargetEvent event) {
			}

			@Override
			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					if (event.data instanceof String[]) {
						String[] files = (String[]) event.data;
						for (String file : files) {
							if (file.toLowerCase().endsWith(".jpg") || file.toLowerCase().endsWith(".jpeg")) {
								MainWindow.this.fileList.add(file);
							}
						}
					}
				}
			}

			@Override
			public void dragOver(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}

			@Override
			public void dragOperationChanged(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}

			@Override
			public void dragLeave(DropTargetEvent event) {
			}

			@Override
			public void dragEnter(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}
		});

		Button selectPics = new Button(composite, SWT.PUSH);
		selectPics.setText("Bilder wählen");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectPics);

		Button removePictures = new Button(composite, SWT.PUSH);
		removePictures.setText("Auswahl löschen");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(removePictures);
		removePictures.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null != MainWindow.this.fileList.getSelectionIndices()) {
					MainWindow.this.fileList.remove(MainWindow.this.fileList.getSelectionIndices());
				}
			}
		});

		Label labelEvenDate = new Label(composite, SWT.NONE);
		labelEvenDate.setText("Eventdatum");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEvenDate);

		this.eventDate = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.eventDate.setSelection(new Date());
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.eventDate);

		Label labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText("");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		Button selectGallery = new Button(composite, SWT.PUSH);
		selectGallery.setText("Galerie wählen");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectGallery);
		selectGallery.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final UserPassDialog userPassDialog = new UserPassDialog(MainWindow.this.getShell());
				if (Dialog.OK == userPassDialog.open()) {
					new Thread() {
						@Override
						public void run() {
							if (TrmEngine.getInstance().connect()) {
								MainWindow.this.addStatusLine("Verbindung wurde hergestellt.");
								if (TrmEngine.getInstance().login(userPassDialog.getUserName(),
									userPassDialog.getPassWord())) {
									MainWindow.this.addStatusLine("Login erfolgreich durgeführt.");
									GalleryDialog galleryDialog = new GalleryDialog(MainWindow.this.getShell(),
										MainWindow.this.getGalleryDate());
								} else {
									MainWindow.this.addStatusLine("Login nicht erfolgreich durgeführt.");
								}
							} else {
								MainWindow.this.addStatusLine("Konnte Verbindung nicht herstellen.");
							}
						}
					}.start();
				}
			}
		});

		Label labelEventLocation = new Label(composite, SWT.NONE);
		labelEventLocation.setText("Eventlocation:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventLocation);

		this.fieldLocation = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldLocation);
		this.fieldLocation.setEditable(false);

		Label labelEventTitle = new Label(composite, SWT.NONE);
		labelEventTitle.setText("EventTitel:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventTitle);

		this.fieldTitle = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldTitle);
		this.fieldTitle.setEditable(false);

		Label labelEventDescription = new Label(composite, SWT.NONE);
		labelEventDescription.setText("EventBeschreibung:");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventDescription);

		this.fieldDesc = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(120, 60).applyTo(this.fieldDesc);
		this.fieldDesc.setEditable(false);

		labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText("");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		this.fieldIntern = new Button(composite, SWT.CHECK);
		this.fieldIntern.setText("Intern");
		this.fieldIntern.setEnabled(false);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldIntern);
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

		this.statusList = new List(composite, SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(290, 340).applyTo(this.statusList);
		Menu statusListMenu = new Menu(this.getShell(), SWT.POP_UP);
		MenuItem statusListMenuItemCopy = new MenuItem(statusListMenu, SWT.PUSH);
		statusListMenuItemCopy.setText("Log kopieren");
		statusListMenuItemCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String logText = StringUtils.join(MainWindow.this.statusList.getItems(), System
					.getProperty("line.separator"));
				Clipboard cb = new Clipboard(Display.getCurrent());
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { logText }, new Transfer[] { textTransfer });
				cb.dispose();
			}
		});
		this.statusList.setMenu(statusListMenu);
		this.addStatusLine("Copyright by ibTEC Team-Ulm GbR");

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
		buttonReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.reset();
			}
		});
		return composite;
	}

	public void addStatusLine(final String line) {
		this.getShell().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				MainWindow.this.statusList.add(line);
				MainWindow.this.statusList.select(statusList.getItems().length - 1);
				MainWindow.this.statusList.showSelection();
				MainWindow.this.statusList.deselectAll();
			}
		});
	}

	private String getGalleryDate() {
		DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
		return format.format(this.eventDate.getSelection());
	}

	public void setConvertProgress(int progress) {
		this.convertProgress.setProgress(progress);
	}

	public void setUploadProgress(int progress) {
		this.uploadProgress.setProgress(progress);
	}

	public void setSelectedPicText(String text) {
		this.selectedPics.setText(text);
	}

	private void reset() {
		// TODO
		this.fileList.removeAll();
	}

	public Gallery getGallery() {
		if (this.gallery.isNewGallery()) {
			this.gallery.setTitle(this.fieldTitle.getText());
			this.gallery.setDesc(this.fieldDesc.getText());
			this.gallery.setIntern(this.fieldIntern.getSelection());
		}
		return this.gallery;
	}

	public void setGallery(Gallery gallery) {
		this.fieldLocation.setText(gallery.getLocation());
		this.fieldTitle.setText(gallery.getTitle());
		this.fieldDesc.setText(gallery.getDesc());
		this.fieldIntern.setSelection(gallery.isIntern());
		if (gallery.isNewGallery()) {
			this.fieldTitle.setEnabled(true);
			this.fieldDesc.setEnabled(true);
			this.fieldIntern.setEnabled(true);
		} else {
			this.fieldTitle.setEnabled(false);
			this.fieldDesc.setEnabled(false);
			this.fieldIntern.setEnabled(false);
		}
		this.gallery = gallery;
	}

}
