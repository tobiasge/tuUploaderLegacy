package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;
import java.io.File;

import com.teamulm.uploadsystem.client.Helper;

public abstract class ImageConverter {

	protected Dimension smallPicSize;

	protected Dimension bigPicSize;

	protected Dimension bigSLRPicSize;

	protected ImageConverter(Dimension smallPicSize, Dimension bigPicSize) {
		this.initSizes(smallPicSize, bigPicSize);
	}

	protected void initSizes(Dimension smallPicSize, Dimension bigPicSize) {
		this.smallPicSize = smallPicSize;
		this.bigPicSize = bigPicSize;
		int bigSLRWidth = bigPicSize.width;
		int bigSLRHeight = (int) ((double) 2 / (double) 3 * (double) bigSLRWidth);
		this.bigSLRPicSize = new Dimension(bigSLRWidth, bigSLRHeight);
	}

	protected boolean isSLRPicture(int width, int height) {
		double aspectRatio = (double) width / (double) height;
		double diff = Math.abs(aspectRatio - ((double) 3 / (double) 2));
		return diff < (double) 0.05;
	}

	protected boolean isDefaultPicture(int width, int height) {
		double aspectRatio = (double) width / (double) height;
		double diff = Math.abs(aspectRatio - ((double) 4 / (double) 3));
		return diff < (double) 0.05;
	}

	protected boolean isUprightPicture(int width, int height) {
		return height > width;
	}

	public boolean isKownFormat(int width, int height) {
		return this.isDefaultPicture(width, height) || this.isSLRPicture(width, height)
				|| this.isUprightPicture(width, height);
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

	public abstract boolean createPic(File inFile, File outFile);

	public abstract boolean createPreview(File inFile, File outFile);

}
