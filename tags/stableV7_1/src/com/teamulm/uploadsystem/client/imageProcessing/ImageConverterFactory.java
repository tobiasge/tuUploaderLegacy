package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Dimension;

public class ImageConverterFactory {

	public static ImageConverter getConverter(Dimension smallPicSize, Dimension bigPicSize) {
		return new NoBlackBorderConverter(smallPicSize, bigPicSize, null, null);
	}

	public static ImageConverter getConverter(Dimension smallPicSize, Dimension bigPicSize, Dimension minHqPicSize,
		Dimension maxHqPicSize) {
		return new NoBlackBorderConverter(smallPicSize, bigPicSize, minHqPicSize, maxHqPicSize);
	}
}
