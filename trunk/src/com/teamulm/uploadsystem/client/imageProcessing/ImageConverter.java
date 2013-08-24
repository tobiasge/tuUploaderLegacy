package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import mediautil.image.jpeg.LLJTran;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.teamulm.uploadsystem.client.Helper;

public abstract class ImageConverter {

	protected Dimension bigPicSize;

	protected Dimension bigSLRPicSize;

	protected Dimension hqSLRPicSize;

	protected Dimension maxHqPicSize;

	protected Dimension minHqPicSize;

	protected Dimension smallPicSize;

	protected ImageConverter(Dimension smallPicSize, Dimension bigPicSize, Dimension minHqPicSize,
		Dimension maxHqPicSize) {
		this.initSizes(smallPicSize, bigPicSize, minHqPicSize, maxHqPicSize);
	}

	public File correctPictureOrientation(File picture) {
		try {
			Metadata metadata = JpegMetadataReader.readMetadata(picture);
			Directory exifDirectory = metadata.getDirectory(ExifIFD0Directory.class);
			int orientation = Integer.parseInt(exifDirectory.getString(ExifIFD0Directory.TAG_ORIENTATION));
			if (1 == orientation) {
				return picture;
			}
			File tmpImg = File.createTempFile("Team-Ulm.de-Uploader-Tmp-Pic-", ".jpg");
			Helper.getInstance().copyFile(picture, tmpImg);
			tmpImg.deleteOnExit();

			// Reduce Log-level for mediautil
			mediautil.gen.Log.debugLevel = mediautil.gen.Log.LEVEL_ERROR;
			LLJTran llj = new LLJTran(tmpImg);
			llj.read(LLJTran.READ_ALL, true);
			int lljOptions = LLJTran.OPT_DEFAULTS | LLJTran.OPT_XFORM_ORIENTATION;
			switch (orientation) {
			case 6: // 90 deg clockwise rotation
				llj.transform(LLJTran.ROT_90, lljOptions);
				break;
			case 3: // 180 deg rotation
				llj.transform(LLJTran.ROT_180, lljOptions);
				break;
			case 8: // 270 deg rotation
				llj.transform(LLJTran.ROT_270, lljOptions);
				break;
			default:
				return null;
			}
			OutputStream out = new BufferedOutputStream(new FileOutputStream(tmpImg.getPath()));
			llj.save(out, LLJTran.OPT_WRITE_ALL);
			out.close();
			return tmpImg;
		} catch (Exception e) {
			Helper.getInstance().systemCrashHandler(e);
			return null;
		}
	}

	public abstract boolean createHqPic(File inFile, File outFile);

	public abstract boolean createPic(File inFile, File outFile);

	public abstract boolean createPreview(File inFile, File outFile);

	public boolean isKownFormat(int width, int height) {
		return this.isDefaultPicture(width, height) || this.isSLRPicture(width, height)
			|| this.isUprightPicture(width, height);
	}

	public boolean isPicBigEnough(BufferedImage pic) {
		if (null == this.maxHqPicSize) {
			return pic.getWidth() >= this.bigPicSize.width && pic.getHeight() >= this.bigPicSize.height;
		} else {
			return pic.getWidth() >= this.minHqPicSize.width && pic.getHeight() >= this.minHqPicSize.height;
		}

	}

	protected void initSizes(Dimension smallPicSize, Dimension bigPicSize, Dimension minHqPicSize,
		Dimension maxHqPicSize) {
		this.smallPicSize = smallPicSize;
		this.bigPicSize = bigPicSize;
		int bigSLRWidth = bigPicSize.width;
		int bigSLRHeight = (int) ((double) 2 / (double) 3 * (double) bigSLRWidth);
		this.bigSLRPicSize = new Dimension(bigSLRWidth, bigSLRHeight);
		if (null != maxHqPicSize) {
			this.maxHqPicSize = maxHqPicSize;
			this.minHqPicSize = minHqPicSize;
			int hqSLRWidth = maxHqPicSize.width;
			int hqSLRHeight = (int) ((double) 2 / (double) 3 * (double) hqSLRWidth);
			this.hqSLRPicSize = new Dimension(hqSLRWidth, hqSLRHeight);
		}
	}

	protected boolean isDefaultPicture(int width, int height) {
		double aspectRatio = (double) width / (double) height;
		double diff = Math.abs(aspectRatio - ((double) 4 / (double) 3));
		return diff < (double) 0.05;
	}

	protected boolean isSLRPicture(int width, int height) {
		double aspectRatio = (double) width / (double) height;
		double diff = Math.abs(aspectRatio - ((double) 3 / (double) 2));
		return diff < (double) 0.05;
	}

	protected boolean isUprightPicture(int width, int height) {
		return height > width;
	}

	protected BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;

		// use PixelGrabber to get at the data...
		int w = image.getWidth(null);
		int h = image.getHeight(null);
		if ((w < 0) || (h < 0))
			return null;
		int[] pixels = new int[w * h];
		PixelGrabber pg = new PixelGrabber(image, 0, 0, w, h, pixels, 0, w);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			Helper.getInstance().systemCrashHandler(e);
			return null;
		}
		if ((pg.getStatus() & ImageObserver.ABORT) != 0)
			return null;
		BufferedImage tmp = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		for (int j = 0; j < h; j++) {
			int row = j * w;
			for (int i = 0; i < w; i++)
				tmp.setRGB(i, j, pixels[row + i]);
		}
		return tmp;
	}

}
