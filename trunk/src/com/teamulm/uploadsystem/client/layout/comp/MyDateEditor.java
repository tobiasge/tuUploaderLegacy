package com.teamulm.uploadsystem.client.layout.comp;

import java.awt.Font;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;

@SuppressWarnings("serial")
public class MyDateEditor extends JSpinner {

	private SpinnerDateModel dateModel;

	private DateEditor dateEditor;

	public MyDateEditor() {
		this.dateModel = new SpinnerDateModel();
		this.setModel(this.dateModel);
		dateEditor = new DateEditor(this, "dd.MM.yyyy");
		dateEditor.setFont(new Font("", Font.PLAIN, 12));
		this.setFont(new Font("", Font.PLAIN, 12));
		this.setEditor(dateEditor);
	}

	public String getDate() {
		String retVal = "";
		GregorianCalendar date2 = new GregorianCalendar();
		date2.setTime(this.dateModel.getDate());
		int day = date2.get(Calendar.DAY_OF_MONTH);
		int month = date2.get(Calendar.MONTH) + 1;
		int year = date2.get(Calendar.YEAR);
		if (day < 10) {
			retVal += "0" + day + "-";
		} else {
			retVal += day + "-";
		}
		if (month < 10) {
			retVal += "0" + month + "-";
		} else {
			retVal += month + "-";
		}
		retVal += year;
		return retVal;
	}

	public boolean isToday() {
		GregorianCalendar today = new GregorianCalendar();
		GregorianCalendar date = new GregorianCalendar();
		date.setTime(this.dateModel.getDate());
		if ((date.get(Calendar.DAY_OF_MONTH) == today
				.get(Calendar.DAY_OF_MONTH))
				&& (date.get(Calendar.MONTH) == today.get(Calendar.MONTH))
				&& (date.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
			return true;
		}
		return false;
	}

	public void setDateToday() {
		this.dateModel.setValue(Calendar.getInstance().getTime());
	}
}
