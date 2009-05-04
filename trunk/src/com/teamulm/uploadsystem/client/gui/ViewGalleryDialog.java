package com.teamulm.uploadsystem.client.gui;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.gallery.AbstractGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryGroupRenderer;
import org.eclipse.nebula.widgets.gallery.DefaultGalleryItemRenderer;
import org.eclipse.nebula.widgets.gallery.GalleryItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.teamulm.uploadsystem.data.Gallery;

public class ViewGalleryDialog extends Dialog {

	private static final Logger log = Logger.getLogger(ViewGalleryDialog.class);

	private Gallery gallery;

	private List<GalleryItem> galleryGroups = null;

	private List<File> tmpFiles = null;

	private List<Image> tmpImages = null;

	private Image iconCross = null;

	public ViewGalleryDialog(Shell parentShell, Gallery gallery) {
		super(parentShell);
		this.gallery = gallery;
		this.galleryGroups = new ArrayList<GalleryItem>();
		this.tmpFiles = new ArrayList<File>();
		this.tmpImages = new ArrayList<Image>();
		this.iconCross = new Image(Display.getCurrent(), "icons/cross.png");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		this.getShell().setText(this.gallery.getLocation() + " am " + this.gallery.getDate().replace('-', '.'));
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		final org.eclipse.nebula.widgets.gallery.Gallery galleryViewer = new org.eclipse.nebula.widgets.gallery.Gallery(
			composite, SWT.VIRTUAL | SWT.V_SCROLL | SWT.BORDER);
		GridDataFactory.fillDefaults().hint(590, 900).grab(true, true).applyTo(galleryViewer);
		galleryViewer.addListener(SWT.SetData, new Listener() {
			public void handleEvent(Event event) {
				final GalleryItem item = (GalleryItem) event.item;
				int tmpIndex = galleryViewer.indexOf(item);
				if (null != item.getParentItem()) {
					GalleryItem parent = item.getParentItem();
					tmpIndex = (((Integer) parent.getData("pageNumber")) * 80) + (tmpIndex + 1);
				} else {
					return;
				}
				final int index = tmpIndex;
				Thread picLoader = new Thread() {
					@Override
					public void run() {
						try {
							URL picUrl = new URL("http://www.team-ulm.de/fotos/parties/"
								+ ViewGalleryDialog.this.gallery.getLocation() + "/"
								+ ViewGalleryDialog.this.gallery.getDate() + "/s_pic" + index + ".jpg");
							URLConnection connection = picUrl.openConnection();
							InputStream inputStream = connection.getInputStream();
							final File tmpPic = File.createTempFile("tu_s_pic", ".jpg");
							FileOutputStream fos = new FileOutputStream(tmpPic);
							int readByte = -1;
							do {
								readByte = inputStream.read();
								fos.write(readByte);
							} while (readByte > -1);
							inputStream.close();
							fos.flush();
							fos.close();
							ViewGalleryDialog.this.tmpFiles.add(tmpPic);
							Display.getDefault().asyncExec(new Runnable() {
								@Override
								public void run() {
									Image pic = new Image(Display.getDefault(), tmpPic.getAbsolutePath());
									if (null != pic) {
										ViewGalleryDialog.this.tmpImages.add(pic);
										if (!item.isDisposed()) {
											item.setImage(pic);
											if (ViewGalleryDialog.this.gallery.isPictureDeleted(index)) {
												item.setData(AbstractGalleryItemRenderer.OVERLAY_BOTTOM_RIGHT,
													ViewGalleryDialog.this.iconCross);
											}
										}
									}
								}
							});
						} catch (MalformedURLException malformedUrlException) {
							log.error("", malformedUrlException);
						} catch (IOException ioException) {
							log.error("", ioException);
						}
					}
				};
				picLoader.setDaemon(true);
				picLoader.start();
			}
		});

		for (int i = 0; i <= (Math.floor(this.gallery.getPictures() / 80)); i++) {
			GalleryItem item = new GalleryItem(galleryViewer, SWT.NONE);
			item.setText("Seite " + (i + 1));
			item.setItemCount(80);
			item.setData("pageNumber", Integer.valueOf(i));
			if (i == (int) Math.floor(this.gallery.getPictures() / 80)) {
				item.setItemCount(this.gallery.getPictures() - 80 * i);
			}
		}

		DefaultGalleryGroupRenderer gr = new DefaultGalleryGroupRenderer();
		gr.setItemSize(134, 100);
		gr.setMinMargin(10);
		galleryViewer.setGroupRenderer(gr);

		DefaultGalleryItemRenderer ir = new DefaultGalleryItemRenderer();
		galleryViewer.setItemRenderer(ir);
		return composite;
	}

	@Override
	protected Control createButtonBar(Composite parent) {
		Control control = super.createButtonBar(parent);
		this.getButton(IDialogConstants.OK_ID).setVisible(false);
		this.getButton(IDialogConstants.CANCEL_ID).setText("Schließen");
		return control;
	}

	@Override
	public boolean close() {
		for (Image image : this.tmpImages) {
			image.dispose();
		}
		this.tmpImages.clear();
		for (File file : this.tmpFiles) {
			file.delete();
		}
		this.tmpFiles.clear();
		this.iconCross.dispose();
		return super.close();
	}

}
