package com.teamulm.uploadsystem.client;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Helper {

	private static Helper instance;

	public static Helper getInstance() {
		if (null == Helper.instance) {
			Helper.instance = new Helper();
		}
		return Helper.instance;
	}

	public String[] readFileData(String fileName, boolean reportError) {
		ArrayList<String> listData = new ArrayList<String>();
		BufferedReader inputStream = null;
		String inDataStr = null;
		try {
			inputStream = new BufferedReader(new InputStreamReader(
					new FileInputStream(fileName)));
			while ((inDataStr = inputStream.readLine()) != null)
				listData.add(inDataStr);
			inputStream.close();
		} catch (Exception ex) {
			if (reportError)
				TeamUlmUpload.getInstance().systemCrashHandler(ex);
			return null;
		}
		Object[] list = listData.toArray();
		String[] ret = new String[list.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = (String) list[i];
		return ret;
	}
}
