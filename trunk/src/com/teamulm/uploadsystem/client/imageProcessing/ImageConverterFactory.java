package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Dimension;

public class ImageConverterFactory {

	public static ImageConverter getConverter(Dimension smallPicSize, Dimension bigPicSize) {
		// return new DefaultImageConverter(smallPicSize, bigPicSize);
		return new NoBlackBorderConverter(smallPicSize, bigPicSize, null);
	}

	public static ImageConverter getConverter(Dimension smallPicSize, Dimension bigPicSize, Dimension hqPicSize) {
		return new NoBlackBorderConverter(smallPicSize, bigPicSize, hqPicSize);
	}
}
