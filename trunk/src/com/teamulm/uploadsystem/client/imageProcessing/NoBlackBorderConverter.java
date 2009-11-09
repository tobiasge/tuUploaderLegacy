package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.gui.Messages;

public class NoBlackBorderConverter extends ImageConverter {

	private static final Logger log = Logger.getLogger(DefaultImageConverter.class);

	public NoBlackBorderConverter(Dimension smallPicSize, Dimension bigPicSize) {
		super(smallPicSize, bigPicSize);
	}

	public boolean createPic(File inFile, File outFile) {
		try {
			BufferedImage pic = this.readImage(inFile);
			if (this.isSLRPicture(pic.getWidth(), pic.getHeight())) {
				log.debug("SLR picture found."); //$NON-NLS-1$
				BufferedImage target = new BufferedImage(this.bigPicSize.width, this.bigPicSize.height,
					BufferedImage.TYPE_INT_RGB);
				Graphics graf = target.getGraphics();
				graf.setColor(Color.BLACK);
				graf.drawRect(0, 0, this.bigPicSize.width, this.bigPicSize.height);
				Image temp = pic.getScaledInstance(this.bigSLRPicSize.width, this.bigSLRPicSize.height,
					BufferedImage.SCALE_SMOOTH);
				int yMove = (int) (((double) bigPicSize.height - (double) bigSLRPicSize.height) / (double) 2);
				graf.drawImage(temp, 0, yMove, null);
				graf.dispose();
				this.writeImage(outFile, target);
				return true;
			} else if (this.isUprightPicture(pic.getWidth(), pic.getHeight())) {
				log.debug("Upright picture found."); //$NON-NLS-1$
				double downRatio = (double) this.bigPicSize.width / (double) pic.getHeight();
				int newWidth = (int) ((double) pic.getWidth() * downRatio);
				this.writeImage(outFile, this.toBufferedImage(pic.getScaledInstance(newWidth,
					this.bigPicSize.width, BufferedImage.SCALE_SMOOTH)));
				return true;
			} else if (this.isDefaultPicture(pic.getWidth(), pic.getHeight())) {
				log.debug("Normal picture found."); //$NON-NLS-1$
				this.writeImage(outFile, this.toBufferedImage(pic.getScaledInstance(this.bigPicSize.width,
					this.bigPicSize.height, BufferedImage.SCALE_SMOOTH)));
				return true;
			} else {
				TeamUlmUpload.getInstance().getMainWindow().addStatusLine(
					Messages.getString("DefaultImageConverter.logMessages.wrongFormat")); //$NON-NLS-1$
				return false;
			}
		} catch (IOException e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}

	private BufferedImage readImage(File img) throws IOException {
		return ImageIO.read(img);
	}

	private void writeImage(File outFile, BufferedImage img) throws IOException {
		Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName("jpeg"); //$NON-NLS-1$
		ImageWriter writer = (ImageWriter) iter.next();
		ImageWriteParam iwp = writer.getDefaultWriteParam();
		iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
		iwp.setCompressionQuality(0.9F);
		FileImageOutputStream output = new FileImageOutputStream(outFile);
		writer.setOutput(output);
		IIOImage image = new IIOImage(img, null, null);
		writer.write(null, image, iwp);
	}

	public boolean createPreview(File inFile, File outFile) {
		try {
			BufferedImage pic = this.readImage(inFile);
			if (this.isUprightPicture(pic.getWidth(), pic.getHeight())) {
				BufferedImage target = new BufferedImage(this.smallPicSize.width, this.smallPicSize.height,
					BufferedImage.TYPE_INT_RGB);
				double downRatio = (double) this.smallPicSize.width / (double) pic.getHeight();
				int newWidth = (int) ((double) pic.getWidth() * downRatio);
				Graphics graf = target.getGraphics();
				graf.setColor(Color.BLACK);
				graf.drawRect(0, 0, this.smallPicSize.width, this.smallPicSize.height);
				Image temp = pic.getScaledInstance(newWidth, this.smallPicSize.height,
					BufferedImage.SCALE_SMOOTH);
				int xMove = (int) (((double) smallPicSize.width - newWidth) / (double) 2);
				graf.drawImage(temp, xMove, 0, null);
				graf.dispose();
				this.writeImage(outFile, target);
				return true;
			} else {
				this.writeImage(outFile, this.toBufferedImage(pic.getScaledInstance(this.smallPicSize.width,
					this.smallPicSize.height, BufferedImage.SCALE_SMOOTH)));
				return true;
			}
		} catch (IOException e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}
}
