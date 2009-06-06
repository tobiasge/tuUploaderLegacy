package com.teamulm.uploadsystem.client.gui;

import java.io.File;
import java.text.Collator;
import java.text.MessageFormat;
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
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
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
import org.joda.time.LocalDate;

import com.teamulm.uploadsystem.client.transmitEngine.TrmEngine;
import com.teamulm.uploadsystem.data.Gallery;

public class MainWindow extends Window {

	private final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MainWindow.class);

	public static final int PROGRESS_BAR_MAX = 1000;

	private CDateTime eventDate = null;

	private Button fieldIntern = null;

	private Text fieldTitle = null, fieldDesc = null, fieldLocation = null;

	private List fileList = null, statusList = null;

	private Gallery gallery = null;

	private Label selectedPics = null;

	private ProgressBar uploadProgressBar = null, convertProgressBar = null;

	public MainWindow() {
		super((Shell) null);
		int style = this.getShellStyle();
		style &= ~(SWT.MAX | SWT.RESIZE);
		this.setShellStyle(style);
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

	@Override
	public boolean close() {
		TrmEngine.getInstance().disconnect();
		return super.close();
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.getString("mainWindow.dialog.title")); //$NON-NLS-1$
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

	public Gallery getGallery() {
		if (this.gallery.isNewGallery()) {
			this.gallery.setTitle(this.fieldTitle.getText());
			this.gallery.setDesc(this.fieldDesc.getText());
			this.gallery.setIntern(this.fieldIntern.getSelection());
		}
		return this.gallery;
	}

	private LocalDate getGalleryDate() {
		return LocalDate.fromDateFields(this.eventDate.getSelection());
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

			public void dragEnter(DropTargetEvent event) {
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

			public void dragOver(DropTargetEvent event) {
				if (!FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					event.detail = DND.DROP_NONE;
				} else {
					event.detail = DND.DROP_COPY;
				}
			}

			public void drop(DropTargetEvent event) {
				if (FileTransfer.getInstance().isSupportedType(event.currentDataType)) {
					if (event.data instanceof String[]) {
						String[] files = (String[]) event.data;
						for (String file : files) {
							if (file.toLowerCase().endsWith(".jpg") || file.toLowerCase().endsWith(".jpeg")) { //$NON-NLS-1$ //$NON-NLS-2$
								MainWindow.this.fileList.add(file);
							}
						}
						MainWindow.this.reOrganiseFileList();
					}
				}
			}
		});
		this.reOrganiseFileList();

		Button selectPics = new Button(composite, SWT.PUSH);
		selectPics.setText(Messages.getString("MainWindow.button.selectPics.text")); //$NON-NLS-1$
		selectPics.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (null != MainWindow.this.fileList.getSelectionIndices()) {
					FileDialog fileDialog = new FileDialog(MainWindow.this.getShell(), SWT.OPEN | SWT.MULTI);
					fileDialog.setFilterExtensions(new String[] { "*.jpg;*.jpeg;*.JPG;*.JPEG" }); //$NON-NLS-1$
					fileDialog.setFilterNames(new String[] { Messages.getString("MainWindow.fileDialog.filter.name") }); //$NON-NLS-1$
					fileDialog.setText(Messages.getString("MainWindow.fileDialog.title")); //$NON-NLS-1$
					if (null == fileDialog.open() || null == fileDialog.getFileNames()) {
						return;
					}
					String filterPath = fileDialog.getFilterPath() + System.getProperty("file.separator"); //$NON-NLS-1$
					for (String fileName : fileDialog.getFileNames()) {
						MainWindow.this.fileList.add(filterPath + fileName);
					}
					MainWindow.this.reOrganiseFileList();
				}
			}
		});
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(selectPics);

		Button removePictures = new Button(composite, SWT.PUSH);
		removePictures.setText(Messages.getString("MainWindow.button.removePictures.text")); //$NON-NLS-1$
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
		labelEvenDate.setText(Messages.getString("MainWindow.label.eventDate.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEvenDate);

		this.eventDate = new CDateTime(composite, CDT.BORDER | CDT.DROP_DOWN | CDT.DATE_MEDIUM);
		this.selectYesterDay();
		this.eventDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent selectionEvent) {
				if (null != MainWindow.this.gallery) {
					if (!MainWindow.this.gallery.getDate().equals(MainWindow.this.getGalleryDate())) {
						MainWindow.this.setGallery(null);
					}
				}
			}
		});
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.eventDate);

		Label labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText(""); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		Button selectGallery = new Button(composite, SWT.PUSH);
		selectGallery.setText(Messages.getString("MainWindow.label.selectGallery.text")); //$NON-NLS-1$
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
								MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.connectOk")); //$NON-NLS-1$
							} else {
								MainWindow.this
									.addStatusLine(Messages.getString("MainWindow.logMessages.connectNotOk")); //$NON-NLS-1$
							}
							if (!TrmEngine.getInstance().isConnected()) {
								return;
							}
							if (TrmEngine.getInstance().isLoggedIn()) {
							} else if (TrmEngine.getInstance().login(userPassDialog.getUserName(),
								userPassDialog.getPassWord())) {
								MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.loginOk")); //$NON-NLS-1$
							} else {
								MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.loginNotOk")); //$NON-NLS-1$
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
		labelEventLocation.setText(Messages.getString("MainWindow.label.eventLocation.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventLocation);

		this.fieldLocation = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldLocation);
		this.fieldLocation.setEditable(false);

		Label labelEventTitle = new Label(composite, SWT.NONE);
		labelEventTitle.setText(Messages.getString("MainWindow.label.eventTitle.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventTitle);

		this.fieldTitle = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.fieldTitle);
		this.fieldTitle.setEditable(false);
		this.fieldTitle.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent verifyEvent) {
				String text = ((Text) verifyEvent.widget).getText();
				if (GalleryDialog.TITLEMAXLENGTH < text.length()) {
					if (verifyEvent.character != SWT.BS && verifyEvent.character != SWT.DEL) {
						verifyEvent.doit = false;
					}
				}
			}
		});

		Label labelEventDescription = new Label(composite, SWT.NONE);
		labelEventDescription.setText(Messages.getString("MainWindow.label.eventDesc.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelEventDescription);

		this.fieldDesc = new Text(composite, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(120, 60).applyTo(this.fieldDesc);
		this.fieldDesc.setEditable(false);
		this.fieldDesc.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent verifyEvent) {
				String text = ((Text) verifyEvent.widget).getText();
				if (GalleryDialog.DESCRMAXLENGTH < text.length()) {
					if (verifyEvent.character != SWT.BS && verifyEvent.character != SWT.DEL) {
						verifyEvent.doit = false;
					}
				}
			}
		});

		labelTmp = new Label(composite, SWT.NONE);
		labelTmp.setText(""); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelTmp);

		this.fieldIntern = new Button(composite, SWT.CHECK);
		this.fieldIntern.setText(Messages.getString("MainWindow.button.internal.text")); //$NON-NLS-1$
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
		this.selectedPics.setText(MessageFormat.format(Messages.getString("MainWindow.label.selectedPics.text"), //$NON-NLS-1$
			Integer.valueOf(files.size())));
	}

	private void reset() {
		this.convertProgressBar.setSelection(0);
		this.uploadProgressBar.setSelection(0);
		this.setGallery(null);
		this.fileList.removeAll();
		this.reOrganiseFileList();
		this.selectYesterDay();
		TrmEngine.getInstance().disconnect();
		this.addStatusLine(Messages.getString("MainWindow.logMessages.programReset")); //$NON-NLS-1$
	}

	private Composite rightComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).equalWidth(false).applyTo(composite);

		Label labelConvertProgress = new Label(composite, SWT.NONE);
		labelConvertProgress.setText(Messages.getString("MainWindow.label.convertProgress.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelConvertProgress);

		this.convertProgressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.convertProgressBar);
		this.convertProgressBar.setMaximum(PROGRESS_BAR_MAX);

		Label labelUploadProgress = new Label(composite, SWT.NONE);
		labelUploadProgress.setText(Messages.getString("MainWindow.label.uploadProgress.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(labelUploadProgress);

		this.uploadProgressBar = new ProgressBar(composite, SWT.HORIZONTAL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(this.uploadProgressBar);
		this.uploadProgressBar.setMaximum(PROGRESS_BAR_MAX);

		this.statusList = new List(composite, SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).hint(290, 340).applyTo(this.statusList);
		Menu statusListMenu = new Menu(this.getShell(), SWT.POP_UP);
		MenuItem statusListMenuItemCopy = new MenuItem(statusListMenu, SWT.PUSH);
		statusListMenuItemCopy.setText(Messages.getString("MainWindow.list.status.menu.copy.text")); //$NON-NLS-1$
		statusListMenuItemCopy.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String logText = StringUtils.join(MainWindow.this.statusList.getItems(), System
					.getProperty("line.separator")); //$NON-NLS-1$
				Clipboard cb = new Clipboard(Display.getCurrent());
				TextTransfer textTransfer = TextTransfer.getInstance();
				cb.setContents(new Object[] { logText }, new Transfer[] { textTransfer });
				cb.dispose();
			}
		});
		this.statusList.setMenu(statusListMenu);
		this.addStatusLine("Copyright by ibTEC Team-Ulm GbR"); //$NON-NLS-1$

		Composite buttonComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(buttonComposite);
		GridLayoutFactory.fillDefaults().equalWidth(false).numColumns(3).applyTo(buttonComposite);

		Button buttonUpload = new Button(buttonComposite, SWT.PUSH);
		buttonUpload.setText(Messages.getString("MainWindow.button.convUpload.text")); //$NON-NLS-1$
		buttonUpload.addSelectionListener(new ConvertUploadListener());
		GridDataFactory.fillDefaults().span(1, 1).grab(false, false).applyTo(buttonUpload);

		Label tmpLabel = new Label(buttonComposite, SWT.NONE);
		tmpLabel.setText("  "); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(true, false).applyTo(tmpLabel);

		Button buttonReset = new Button(buttonComposite, SWT.PUSH);
		buttonReset.setText(Messages.getString("MainWindow.button.reset.text")); //$NON-NLS-1$
		GridDataFactory.fillDefaults().span(1, 1).grab(false, false).applyTo(buttonReset);
		buttonReset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				MainWindow.this.reset();
			}
		});
		return composite;
	}

	private void selectYesterDay() {
		Calendar cal = GregorianCalendar.getInstance();
		cal.clear(Calendar.HOUR);
		cal.clear(Calendar.MINUTE);
		cal.clear(Calendar.SECOND);
		cal.clear(Calendar.MILLISECOND);
		cal.add(Calendar.DAY_OF_MONTH, -1);
		this.eventDate.setSelection(cal.getTime());
	}

	public void setConvertProgress(final int progress) {
		this.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MainWindow.this.convertProgressBar.setSelection(progress);
			}
		});

	}

	public void setGallery(Gallery gallery) {
		log.debug("Setting gallery: " + gallery); //$NON-NLS-1$
		this.gallery = gallery;
		if (null == this.gallery) {
			this.fieldLocation.setText(""); //$NON-NLS-1$
			this.fieldTitle.setText(""); //$NON-NLS-1$
			this.fieldDesc.setText(""); //$NON-NLS-1$
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

	public void setUploadProgress(final int progress) {
		this.getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				MainWindow.this.uploadProgressBar.setSelection(progress);
			}
		});
	}

	private class ConvertUploadListener extends SelectionAdapter {
		@Override
		public void widgetSelected(SelectionEvent arg0) {
			if (0 == MainWindow.this.fileList.getItemCount()) {
				MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.missingFiles")); //$NON-NLS-1$
				return;
			}
			if (null == MainWindow.this.gallery) {
				MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.missingGallery")); //$NON-NLS-1$
				return;
			}
			if (MainWindow.this.gallery.isNewGallery()) {
				if (0 == MainWindow.this.fieldTitle.getText().length()) {
					MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.missingTitle")); //$NON-NLS-1$
					return;
				}
				if (0 == MainWindow.this.fieldDesc.getText().length()) {
					MainWindow.this.addStatusLine(Messages.getString("MainWindow.logMessages.missingDesc")); //$NON-NLS-1$
					return;
				}
			}

			if (!TrmEngine.getInstance().lockLocation(MainWindow.this.gallery)) {
				return;
			}
			File[] files = new File[MainWindow.this.fileList.getItemCount()];
			int index = 0;
			for (String fileName : MainWindow.this.fileList.getItems()) {
				File file = new File(fileName);
				files[index++] = file;
			}
			TrmEngine.getInstance().setFiles(files);
			TrmEngine.getInstance().start();
		}
	}
}
