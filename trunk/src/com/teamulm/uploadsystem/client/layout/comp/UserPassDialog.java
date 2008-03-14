package com.teamulm.uploadsystem.client.layout.comp;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.teamulm.uploadsystem.client.layout.MainWindow;

public class UserPassDialog {

	private static final Logger log = Logger.getLogger(UserPassDialog.class);

	private JTextField userName;

	private JPasswordField passWord;

	private JButton chButton, okButton;

	private int userSaidValue = -1;

	public UserPassDialog() {
		this.userName = new JTextField();
		this.userName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode()) {
					log.debug("return in username");
					UserPassDialog.this.passWord.requestFocusInWindow();
				}
			}
		});
		this.passWord = new JPasswordField();
		this.passWord.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.VK_ENTER == e.getKeyCode()) {
					log.debug("return in password");
					UserPassDialog.this.okButton.doClick();
				}
			}
		});
		okButton = new JButton("Ok");
		okButton.setPreferredSize(new Dimension(90, 23));
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				JOptionPane optPane = (JOptionPane) src.getParent().getParent();
				UserPassDialog.this.userSaidValue = JOptionPane.OK_OPTION;
				optPane.setValue(new Integer(JOptionPane.OK_OPTION));
			}
		});
		chButton = new JButton("Abbrechen");
		chButton.setPreferredSize(new Dimension(90, 23));
		chButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				JButton src = (JButton) e.getSource();
				JOptionPane optPane = (JOptionPane) src.getParent().getParent();
				UserPassDialog.this.userSaidValue = JOptionPane.NO_OPTION;
				optPane.setValue(new Integer(JOptionPane.NO_OPTION));
			}
		});
	}

	public int passDialog() {

		Object[] options = { this.okButton, this.chButton };

		Object[] input = new Object[] { "Username:", this.userName,
				"Passwort:", this.passWord };
		JOptionPane.showOptionDialog(MainWindow.getInstance(), input,
				"Passwort...?", JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE, null, options, this.userName);
		return this.userSaidValue;
	}

	public String getUser() {
		return this.userName.getText();
	}

	public String getPass() {
		return new String(this.passWord.getPassword());
	}
}
