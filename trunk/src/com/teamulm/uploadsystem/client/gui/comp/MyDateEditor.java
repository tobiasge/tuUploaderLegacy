package com.teamulm.uploadsystem.client.gui.comp;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

@SuppressWarnings("serial")
public class MyDateEditor extends JDateChooser {
	private boolean isCalendarVisible = false;

	public MyDateEditor() {
		super(new Date(), "dd.MM.yyyy");
		this.setLocale(Locale.GERMANY);
		this.setMaxSelectableDate(new Date());
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (this.isCalendarVisible) {
			this.popup.setVisible(false);
			this.isCalendarVisible = false;
			return;
		}
		this.isCalendarVisible = true;
		super.actionPerformed(e);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("day")) {
			if (this.popup.isVisible()) {
				this.isCalendarVisible = false;
			}
		}
		super.propertyChange(evt);
	}

	public String getDateString() {
		return ((JTextFieldDateEditor) this.getDateEditor().getUiComponent())
				.getText().replace('.', '-');
	}

	public boolean isToday() {
		Calendar today = new GregorianCalendar();
		Calendar date = this.getCalendar();
		if ((date.get(Calendar.DAY_OF_MONTH) == today
				.get(Calendar.DAY_OF_MONTH))
				&& (date.get(Calendar.MONTH) == today.get(Calendar.MONTH))
				&& (date.get(Calendar.YEAR) == today.get(Calendar.YEAR))) {
			return true;
		}
		return false;
	}

	public void setDateToday() {
		this.setDate(Calendar.getInstance().getTime());
	}
}
