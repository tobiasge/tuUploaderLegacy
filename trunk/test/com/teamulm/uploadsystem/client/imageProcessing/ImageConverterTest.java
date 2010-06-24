package com.teamulm.uploadsystem.client.imageProcessing;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ImageConverterTest {

	private static Dimension bigPicSize;

	private static Logger log = null;

	private static Dimension maxHqPicSize;

	private static Dimension minHqPicSize;

	private static Dimension smallPicSize;

	@BeforeClass
	public static void setUp() throws FileNotFoundException, IOException {
		ImageConverterTest.initLogger();
		ImageConverterTest.clearOldRun();
		ImageConverterTest.initPicSizes();
	}

	@AfterClass
	public static void tearDown() {

	}

	private static void clearOldRun() {
		log.info("Clearing output directory from previous run");
		File testOut = new File("./testOut");
		for (File subFile : testOut.listFiles()) {
			log.info("Deleting " + subFile);
			subFile.delete();
		}
	}

	private static void initLogger() throws FileNotFoundException, IOException {
		ImageConverterTest.log = Logger.getLogger(ImageConverterTest.class);
		Properties logConf = new Properties();
		logConf.load(new FileInputStream("client.log4j.properties")); //$NON-NLS-1$
		PropertyConfigurator.configure(logConf);
	}

	private static void initPicSizes() {
		ImageConverterTest.smallPicSize = new Dimension(134, 100);
		ImageConverterTest.bigPicSize = new Dimension(596, 447);
		ImageConverterTest.maxHqPicSize = new Dimension(1600, 1200);
		ImageConverterTest.minHqPicSize = new Dimension(1200, 900);
	}

	@Test
	public void createHqPictures() {
		ImageConverter converter = new NoBlackBorderConverter(smallPicSize, bigPicSize, minHqPicSize, maxHqPicSize);
	}

	@Test
	public void createNormalPictures() {
		ImageConverter converter = new NoBlackBorderConverter(smallPicSize, bigPicSize, minHqPicSize, maxHqPicSize);
	}

	@Test
	public void createSmallPictures() {
		ImageConverter converter = new NoBlackBorderConverter(smallPicSize, bigPicSize, minHqPicSize, maxHqPicSize);
	}
}
