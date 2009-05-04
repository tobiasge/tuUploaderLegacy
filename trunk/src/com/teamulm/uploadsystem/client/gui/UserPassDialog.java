package com.teamulm.uploadsystem.client.gui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class UserPassDialog extends Dialog {

	private Text textUserName = null;

	private Text textPassWord = null;

	private String userName = null;

	private String passWord = null;

	public UserPassDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		this.getShell().setText(Messages.getString("userPassDialog.dialog.title"));
		GridLayoutFactory.fillDefaults().numColumns(3).equalWidth(false).margins(5, 5).applyTo(composite);
		Label iconLabel = new Label(composite, SWT.NONE);
		iconLabel.setImage(this.getShell().getDisplay().getSystemImage(SWT.ICON_QUESTION));
		GridDataFactory.fillDefaults().span(1, 2).hint(50, SWT.DEFAULT).indent(10, SWT.DEFAULT).applyTo(iconLabel);

		Label labelUserName = new Label(composite, SWT.NONE);
		labelUserName.setText(Messages.getString("userPassDialog.label.username"));
		GridDataFactory.fillDefaults().applyTo(labelUserName);

		this.textUserName = new Text(composite, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).hint(100, SWT.DEFAULT).applyTo(this.textUserName);
		this.textUserName.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent traverseEvent) {
				if (SWT.CR == traverseEvent.character) {
					traverseEvent.doit = false;
					UserPassDialog.this.textPassWord.setFocus();
				}
			}
		});

		Label labelpassWord = new Label(composite, SWT.NONE);
		labelpassWord.setText(Messages.getString("userPassDialog.label.password"));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(labelpassWord);

		this.textPassWord = new Text(composite, SWT.BORDER | SWT.PASSWORD);
		GridDataFactory.fillDefaults().applyTo(this.textPassWord);

		return composite;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassWord() {
		return passWord;
	}

	@Override
	protected void okPressed() {
		this.passWord = this.textPassWord.getText();
		this.userName = this.textUserName.getText();
		super.okPressed();
	}
}
