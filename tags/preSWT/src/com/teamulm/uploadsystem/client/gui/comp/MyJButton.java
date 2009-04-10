package com.teamulm.uploadsystem.client.gui.comp;

import java.awt.Dimension;

import javax.swing.JButton;

@SuppressWarnings("serial")
public class MyJButton extends JButton {

	public MyJButton(String title) {
		super(title);
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(140, 20);
	}

	@Override
	public Dimension getMaximumSize() {
		return new Dimension(140, 20);
	}

	@Override
	public Dimension getMinimumSize() {
		return new Dimension(140, 20);
	}
}
