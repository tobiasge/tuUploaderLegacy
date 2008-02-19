package com.teamulm.uploadsystem.client.wizard;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.pietschy.wizard.AbstractWizardStep;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.WizardModel;

public class LoginStep extends AbstractWizardStep {

	private static final Logger log = Logger.getLogger(LoginStep.class);

	private UploadWizardModel model;

	private boolean isLoggedIn;

	JPanel mainView;
	private JTextField userName;

	private JPasswordField passWord;
	JButton chButton, okButton;

	public LoginStep() {
		super("Login",
				"Bitte Usernamen und Passwort eingeben, um Bilder hochladen zu können.");
		this.isLoggedIn = false;
		this.initMainView();
	}

	public void applyState() throws InvalidStateException {
		if (this.isLoggedIn)
			return;
		setBusy(true);
		LoginStep.log.info("starting transmitter");
		if (!this.model.getTrmEngine().connect()) {
			LoginStep.log.error("connect failed");
		}
		if (!this.model.getTrmEngine().login(this.userName.getText(),
				new String(this.passWord.getPassword()))) {
			setBusy(false);
			throw new InvalidStateException("Username oder Passwort falsch.");
		} else {
			this.isLoggedIn = true;
		}
		setBusy(false);
	}

	public void prepare() {
		setView(this.mainView);
		if (this.isLoggedIn) {
			this.userName.setEnabled(false);
			this.passWord.setEnabled(false);
		} else {
			this.userName.setEnabled(true);
			this.passWord.setEnabled(true);
		}
	}

	@Override
	public void init(WizardModel model) {
		this.model = (UploadWizardModel) model;
	}

	public Dimension getPreferredSize() {
		return new Dimension(200, 300);
	}

	private void initMainView() {
		this.mainView = new JPanel(new GridLayout(14, 2));
		this.userName = new JTextField();
		this.userName.addFocusListener(new TextFieldListener());
		this.userName.addKeyListener(new TextFieldListener());
		this.passWord = new JPasswordField();
		this.passWord.addFocusListener(new TextFieldListener());
		this.passWord.addKeyListener(new TextFieldListener());

		this.mainView.add(new JLabel("Username:"));
		this.mainView.add(this.userName);
		this.mainView.add(new JLabel("Passwort:"));
		this.mainView.add(this.passWord);
	}

	private class TextFieldListener implements FocusListener, KeyListener {

		public void focusGained(FocusEvent e) {
		}

		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			this.setComplete();
		}

		public void keyTyped(KeyEvent e) {
		}

		public void focusLost(FocusEvent e) {
			this.setComplete();
		}

		private void setComplete() {
			if (LoginStep.this.userName == null
					|| LoginStep.this.passWord == null)
				return;
			LoginStep.this.setComplete(LoginStep.this.userName.getText()
					.length() != 0
					&& LoginStep.this.passWord.getPassword().length != 0);
		}
	}
}
