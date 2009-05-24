package com.teamulm.uploadsystem.client.gui;

import java.text.Collator;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeSet;

import org.apache.commons.lang.ArrayUtils;
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
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class MainWindow extends Window {

	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MainWindow.class);

	public static final int PROGRESS_BAR_MAX = 1000;

	private List fileList = null, statusList = null;

	private Text fieldTitle = null, fieldDesc = null, fieldLocation = null;

	private ProgressBar uploadProgressBar = null, convertProgressBar = null;

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
	public boolean close() {
		TrmEngine.getInstance().disconnect();
		return super.close();
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
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).applyTo(this.selectedPics);

		this.fileList = new List(composite, SWT.BORDER | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
		this.fileList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent keyEvent) {
				if (SWT.DEL == keyEvent.character) {
					if (0 < MainWindow.this.fileList.getSelectionIndices().length) {
						MainWindow.this.fileList.remove(MainWindow.this.fileList.getSelectionIndices());
					}
				}
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).grab(true, false).hint(300, 210).applyTo(this.fileList);
		DropTarget fileListTarget = new DropTarget(this.fileList, DND.DROP_COPY);
		fileListTarget.setTransfer(new Transfer[] { FileTransfer.getInstance() });
		fileListTarget.addDropListener(new DropTargetAdapter() {

			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					if (event.data instanceof String[]) {
						String[] files = (String[]) event.data;
						for (String file : files) {
							if (file.toLowerCase().endsWith(".jpg") || file.toLowerCase().endsWith(".jpeg")) {
								MainWindow.this.fileList.add(file);
							}
						}
						MainWindow.this.reOrganiseFileList();
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}

			public void dragOperationChanged(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}

			public void dragEnter(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}
		});
		this.reOrganiseFileList();

		Button selectPics = new Button(composite, SWT.PUSH);
		selectPics.setText(Messages.getString("MainWindow.button.selectPics.text"));
		selectPics.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null != MainWindow.this.fileList.getSelectionIndices()) {
					FileDialog fileDialog = new FileDialog(MainWindow.this.getShell(), SWT.OPEN | SWT.MULTI);
					fileDialog.setFilterExtensions(new String[] { "*.jpg;*.jpeg;*.JPG;*.JPEG" });
					fileDialog.setFilterNames(new String[] { "JPEG Bilddateien" });
					fileDialog.setText("Bilder wählen");
					if (null == fileDialog.open() || null == fileDialog.getFileNames()) {
						return;
					}
					String filterPath = fileDialog.getFilterPath() + System.getProperty("file.separator");
					for (String fileName : fileDialog.getFileNames()) {
						MainWindow.this.fileList.add(filterPath + fileName);
					}
					MainWindow.this.reOrganiseFileList();
				}
			}
		});
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectPics);

		Button removePictures = new Button(composite, SWT.PUSH);
		removePictures.setText(Messages.getString("MainWindow.button.removePictures.text"));
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
		labelEvenDate.setText(Messages.getString("MainWindow.label.eventDate.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEvenDate);

		this.eventDate = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		Calendar cal = GregorianCalendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, -1);
		this.eventDate.setSelection(cal.getTime());
		this.eventDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				if (null != MainWindow.this.gallery) {
					if (!MainWindow.this.gallery.getDate().equalsIgnoreCase(MainWindow.this.getGalleryDate())) {
						MainWindow.this.setGallery(null);
					}
				}
			}
		});
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.eventDate);

		Label labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText("");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		Button selectGallery = new Button(composite, SWT.PUSH);
		selectGallery.setText(Messages.getString("MainWindow.label.selectGallery.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectGallery);
		selectGallery.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final UserPassDialog userPassDialog = new UserPassDialog(MainWindow.this.getShell());
				if (TrmEngine.getInstance().isLoggedIn() || Dialog.OK == userPassDialog.open()) {
					new Thread() {
						@Override
						public void run() {
							if (TrmEngine.getInstance().isConnected()) {
							} else if (TrmEngine.getInstance().connect()) {
								MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.connectOk"));
							} else {
								MainWindow.this
									.addStatusLine(Messages.getString("MainWindow.logMessages.connectNotOk"));
							}
							if (!TrmEngine.getInstance().isConnected()) {
								return;
							}
							if (TrmEngine.getInstance().isLoggedIn()) {
							} else if (TrmEngine.getInstance().login(userPassDialog.getUserName(),
								userPassDialog.getPassWord())) {
								MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.loginOk"));
							} else {
								MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.loginNotOk"));
								return;
							}

							final GalleryDialog galleryDialog = new GalleryDialog(MainWindow.this.getShell(),
								MainWindow.this.getGalleryDate());
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									galleryDialog.open();
								}
							});
						}
					}.start();
				}
			}
		});

		Label labelEventLocation = new Label(composite, SWT.NONE);
		labelEventLocation.setText(Messages.getString("MainWindow.label.eventLocation.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventLocation);

		this.fieldLocation = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldLocation);
		this.fieldLocation.setEditable(false);

		Label labelEventTitle = new Label(composite, SWT.NONE);
		labelEventTitle.setText(Messages.getString("MainWindow.label.eventTitle.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventTitle);

		this.fieldTitle = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldTitle);
		this.fieldTitle.setEditable(false);

		Label labelEventDescription = new Label(composite, SWT.NONE);
		labelEventDescription.setText(Messages.getString("MainWindow.label.eventDesc.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventDescription);

		this.fieldDesc = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(120, 60).applyTo(this.fieldDesc);
		this.fieldDesc.setEditable(false);

		labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText("");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		this.fieldIntern = new Button(composite, SWT.CHECK);
		this.fieldIntern.setText(Messages.getString("MainWindow.button.internal.text"));
		this.fieldIntern.setEnabled(false);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldIntern);
		return composite;
	}

	private void reOrganiseFileList() {
		TreeSet<String> files = new TreeSet<String>(Collator.getInstance());
		for (String file : this.fileList.getItems()) {
			files.add(file);
		}
		this.fileList.setItems(files.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
		this.selectedPics.setText(MessageFormat.format(Messages.getString("MainWindow.label.selectedPics.text"),
			Integer.valueOf(files.size())));
	}

	private Composite rightComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(composite);

		Label labelConvertProgress = new Label(composite, SWT.NONE);
		labelConvertProgress.setText(Messages.getString("MainWindow.label.convertProgress.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelConvertProgress);

		this.convertProgressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.convertProgressBar);
		this.convertProgressBar.setMaximum(PROGRESS_BAR_MAX);

		Label labelUploadProgress = new Label(composite, SWT.NONE);
		labelUploadProgress.setText(Messages.getString("MainWindow.label.uploadProgress.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelUploadProgress);

		this.uploadProgressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.uploadProgressBar);
		this.uploadProgressBar.setMaximum(PROGRESS_BAR_MAX);

		this.statusList = new List(composite, SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(290, 340).applyTo(this.statusList);
		Menu statusListMenu = new Menu(this.getShell(), SWT.POP_UP);
		MenuItem statusListMenuItemCopy = new MenuItem(statusListMenu, SWT.PUSH);
		statusListMenuItemCopy.setText(Messages.getString("MainWindow.list.status.menu.copy.text"));
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
		buttonUpload.setText(Messages.getString("MainWindow.button.convUpload.text"));
		GridDataFactory.fillDefaults().span(1, 1).grab(false, false).applyTo(buttonUpload);

		Label tmpLabel = new Label(buttonComposite, SWT.NONE);
		tmpLabel.setText("  ");
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(tmpLabel);

		Button buttonReset = new Button(buttonComposite, SWT.PUSH);
		buttonReset.setText(Messages.getString("MainWindow.button.reset.text"));
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

	public void setConvertProgress(final int progress) {
		this.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MainWindow.this.convertProgressBar.setSelection(progress);
			}
		});

	}

	public void setUploadProgress(final int progress) {
		this.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MainWindow.this.convertProgressBar.setSelection(progress);
			}
		});
	}

	private void reset() {
		// TODO
		this.setGallery(null);
		this.fileList.removeAll();
		this.addStatusLine(Messages.getString("MainWindow.logMessages.programReset"));
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
		log.debug("Setting gallery: " + gallery);
		this.gallery = gallery;
		if (null == this.gallery) {
			this.fieldLocation.setText("");
			this.fieldTitle.setText("");
			this.fieldDesc.setText("");
			this.fieldIntern.setSelection(false);
		} else {
			this.fieldLocation.setText(this.gallery.getLocation());
			this.fieldTitle.setText(this.gallery.getTitle());
			this.fieldDesc.setText(this.gallery.getDesc());
			this.fieldIntern.setSelection(this.gallery.isIntern());
		}
		if (null != this.gallery && this.gallery.isNewGallery()) {
			this.fieldTitle.setEditable(true);
			this.fieldDesc.setEditable(true);
			this.fieldIntern.setEnabled(true);
		} else {
			this.fieldTitle.setEditable(false);
			this.fieldDesc.setEditable(false);
			this.fieldIntern.setEnabled(false);
		}
	}
}
