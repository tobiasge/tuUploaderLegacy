package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;

import com.teamulm.uploadsystem.client.Helper;

public abstract class ImageConverter {

	protected Point smallPicSize;

	protected Point bigPicSize;

	protected Point bigSLRPicSize;

	protected ImageConverter(Point smallPicSize, Point bigPicSize) {
		this.initSizes(smallPicSize, bigPicSize);
	}

	protected void initSizes(Point smallPicSize, Point bigPicSize) {
		this.smallPicSize = smallPicSize;
		this.bigPicSize = bigPicSize;
		int bigSLRWidth = bigPicSize.x;
		int bigSLRHeight = (int) ((double) 2 / (double) 3 * (double) bigSLRWidth);
		this.bigSLRPicSize = new Point(bigSLRWidth, bigSLRHeight);
	}

	protected boolean isSLRPicture(BufferedImage pic) {
		double aspectRatio = (double) pic.getWidth() / (double) pic.getHeight();
		double diff = Math.abs(aspectRatio - ((double) 3 / (double) 2));
		return diff < (double) 0.05;
	}

	protected boolean isDefaultPicture(BufferedImage pic) {
		double aspectRatio = (double) pic.getWidth() / (double) pic.getHeight();
		double diff = Math.abs(aspectRatio - ((double) 4 / (double) 3));
		return diff < (double) 0.05;
	}

	protected boolean isUprightPicture(BufferedImage pic) {
		return pic.getHeight() > pic.getWidth();
	}

	protected BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage)
			return (BufferedImage) image;
		else { // use PixelGrabber to get at the data...
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

	public abstract BufferedImage createPic(BufferedImage pic);

	public abstract BufferedImage createPreview(BufferedImage pic);

}
