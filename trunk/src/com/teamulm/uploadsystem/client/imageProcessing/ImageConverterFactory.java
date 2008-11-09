package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Point;

public class ImageConverterFactory {

	public static ImageConverter getConverter(Point smallPicSize, Point bigPicSize) {
		return new DefaultImageConverter(smallPicSize, bigPicSize);
	}
}
