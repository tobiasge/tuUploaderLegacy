package com.teamulm.uploadsystem.client.gui.comp;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.teamulm.uploadsystem.client.gui.MainWindow;

@SuppressWarnings("serial")
public class MyJTextField extends JTextField {

	public MyJTextField(int maxLength) {
		super();
		if (maxLength > 0) {
			this.setInputVerifier(new MyInputVerifier(maxLength));
		}
	}

	private class MyInputVerifier extends InputVerifier {

		private int maxLength;

		public MyInputVerifier(int maxLength) {
			super();
			this.maxLength = maxLength;
		}

		@Override
		public boolean verify(JComponent arg0) {
			JTextField lab = (JTextField) arg0;
			if (lab.getText().length() > this.maxLength) {
				JOptionPane.showMessageDialog(MainWindow.getInstance(),
						"<html>Der Text ist zu lang (" + lab.getText().length()
								+ " Zeichen).<br>Bitte kürze ihn um "
								+ (lab.getText().length() - this.maxLength)
								+ " Zeichen.</html>");
				return false;
			} else
				return true;
		}
	}
}
