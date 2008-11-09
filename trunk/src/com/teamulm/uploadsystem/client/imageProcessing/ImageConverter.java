package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.image.BufferedImage;

public interface ImageConverter {

	public BufferedImage createPic(BufferedImage pic);

	public BufferedImage createPreview(BufferedImage pic);

}
