package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.Helper;
import com.teamulm.uploadsystem.client.gui.MainWindow;

public class DefaultImageConverter extends ImageConverter {

	private static final Logger log = Logger.getLogger(DefaultImageConverter.class);

	public DefaultImageConverter(Point smallPicSize, Point bigPicSize) {
		super(smallPicSize, bigPicSize);
	}

	public boolean createPic(File inFile, File outFile) {
		try {
			BufferedImage pic = this.readImage(inFile);
			if (this.isSLRPicture(pic)) {
				log.debug("SLR picture found.");
				BufferedImage target = new BufferedImage(this.bigPicSize.x, this.bigPicSize.y,
						BufferedImage.TYPE_INT_RGB);
				Graphics graf = target.getGraphics();
				graf.setColor(Color.BLACK);
				graf.drawRect(0, 0, this.bigPicSize.x, this.bigPicSize.y);
				Image temp = pic.getScaledInstance(this.bigSLRPicSize.x, this.bigSLRPicSize.y,
						BufferedImage.SCALE_SMOOTH);
				int yMove = (int) (((double) bigPicSize.y - (double) bigSLRPicSize.y) / (double) 2);
				graf.drawImage(temp, 0, yMove, null);
				graf.dispose();
				this.writeImage(outFile, target);
				return true;
			} else if (this.isUprightPicture(pic)) {
				log.debug("Upright picture found.");
				BufferedImage target = new BufferedImage(this.bigPicSize.x, this.bigPicSize.y,
						BufferedImage.TYPE_INT_RGB);
				double downRatio = (double) this.bigPicSize.y / (double) pic.getHeight();
				int newWidth = (int) ((double) pic.getWidth() * downRatio);
				Graphics graf = target.getGraphics();
				graf.setColor(Color.BLACK);
				graf.drawRect(0, 0, this.bigPicSize.x, this.bigPicSize.y);
				Image temp = pic.getScaledInstance(newWidth, this.bigPicSize.y, BufferedImage.SCALE_SMOOTH);
				int xMove = (int) (((double) bigPicSize.x - newWidth) / (double) 2);
				graf.drawImage(temp, xMove, 0, null);
				graf.dispose();
				this.writeImage(outFile, target);
				return true;
			} else if (this.isDefaultPicture(pic)) {
				log.debug("Normal picture found.");
				this.writeImage(outFile, this.toBufferedImage(pic.getScaledInstance(this.bigPicSize.x,
						this.bigPicSize.y, BufferedImage.SCALE_SMOOTH)));
				return true;
			} else {
				MainWindow.getInstance().addStatusLine("Bild ignoriert: Bild hat falsches Format.");
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

	private void writeImage(File imgFile, BufferedImage img) throws IOException {
		ImageIO.write(img, "jpg", imgFile);
	}

	public boolean createPreview(File inFile, File outFile) {
		try {
			this.writeImage(outFile, this.toBufferedImage(this.readImage(inFile).getScaledInstance(this.smallPicSize.x,
					this.smallPicSize.y, BufferedImage.SCALE_SMOOTH)));
			return true;
		} catch (IOException e) {
			Helper.getInstance().systemCrashHandler(e);
			return false;
		}
	}
}
