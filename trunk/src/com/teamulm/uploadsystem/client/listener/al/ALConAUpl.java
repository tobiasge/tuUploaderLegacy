/* ALConAUpl.java
 *
 *******************************************************
 *
 * Beschreibung:
 *
 *
 * Autor: Wolfgang Holoch
 * (C) 2004
 *
 *******************************************************/
package com.teamulm.uploadsystem.client.listener.al;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.TeamUlmUpload;
import com.teamulm.uploadsystem.client.layout.MainWindow;

public class ALConAUpl implements ActionListener {

	private File[] files = null;

	private JTextField userName;

	private JPasswordField passWord;

	JButton chButton, okButton;

	private int userSaidValue = -1;

	private static final Logger log = Logger.getLogger(ALConAUpl.class);

	public void actionPerformed(ActionEvent e) {
		this.userSaidValue = -1;

		this.files = MainWindow.getInstance().getFileList().getFiles();
		if (null == this.files || this.files.length == 0) {
			MainWindow.getInstance().addStatusLine("Keine Dateien ausgewählt.");
			return;
		} else if (null == MainWindow.getInstance().getTMPDir()
				|| MainWindow.getInstance().getTMPDir().length() < 1) {
			MainWindow.getInstance().addStatusLine(
					"Kein Speicherort ausgewählt.");
			return;
		} else if (null == MainWindow.getInstance().getEventTitle()
				|| MainWindow.getInstance().getEventTitle().length() < 1) {
			MainWindow.getInstance().addStatusLine("Bitte Titel angeben.");
			return;
		} else if (null == MainWindow.getInstance().getEventDesc()
				|| MainWindow.getInstance().getEventDesc().length() < 1) {
			MainWindow.getInstance().addStatusLine(
					"Bitte Beschreibung angeben.");
			return;
		}

		if (MainWindow.getInstance().getDateEditor().isToday()) {
			int retVal = this.todayDialog();
			if ((JOptionPane.NO_OPTION == retVal || JOptionPane.CLOSED_OPTION == retVal)) {
				MainWindow.getInstance().addStatusLine("Abgebrochen.");
				return;
			}
		}

		this.passDialog();
		if (JOptionPane.NO_OPTION == this.userSaidValue
				|| JOptionPane.CLOSED_OPTION == userSaidValue) {
			MainWindow.getInstance().addStatusLine("Abgebrochen.");
			return;
		}
		log.debug("starte upload " + this.userSaidValue);

		TeamUlmUpload.getInstance().EngineInit(this.files,
				this.userName.getText(),
				new String(this.passWord.getPassword()));
		TeamUlmUpload.getInstance().engineStart();

	}

	private void passDialog() {
		okButton = new JButton("Ok");
		okButton.setPreferredSize(new Dimension(90, 23));
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				JOptionPane optPane = (JOptionPane) src.getParent().getParent();
				ALConAUpl.this.userSaidValue = JOptionPane.OK_OPTION;
				optPane.setValue(new Integer(JOptionPane.OK_OPTION));
			}
		});
		chButton = new JButton("Abbrechen");
		chButton.setPreferredSize(new Dimension(90, 23));
		chButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				JOptionPane optPane = (JOptionPane) src.getParent().getParent();
				ALConAUpl.this.userSaidValue = JOptionPane.NO_OPTION;
				optPane.setValue(new Integer(JOptionPane.NO_OPTION));
			}
		});
		Object[] options = { okButton, chButton };
		this.userName = new JTextField();
		this.passWord = new JPasswordField();
		this.userName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode()) {
					log.debug("return in username");
					ALConAUpl.this.passWord.requestFocusInWindow();
				}
			}
		});
		this.passWord.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode()) {
					log.debug("return in password");
					ALConAUpl.this.okButton.doClick();
				}
			}
		});
		Object[] input = new Object[] { "Username:", this.userName,
				"Passwort:", this.passWord };
		JOptionPane.showOptionDialog(MainWindow.getInstance(), input,
				"Passwort...?", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, this.userName);
	}

	private int todayDialog() {
		Object[] options = { "Ja", "Nein" };
		return JOptionPane.showOptionDialog(MainWindow.getInstance(),
				"War das Event wirklich heute?", "Datum...?",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null,
				options, options[1]);
	}
}
