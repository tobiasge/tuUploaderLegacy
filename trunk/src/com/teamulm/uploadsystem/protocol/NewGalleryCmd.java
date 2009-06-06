package com.teamulm.uploadsystem.protocol;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.teamulm.uploadsystem.data.Gallery;

public class NewGalleryCmd extends Command {

	private static final long serialVersionUID = 750758930754656727L;

	private Gallery gallery;

	public Gallery getGallery() {
		return gallery;
	}

	public void setGallery(Gallery gallery) {
		this.gallery = gallery;
	}

	public NewGalleryCmd() {
		super();
	}

	public NewGalleryCmd(boolean serverResponse) {
		super(serverResponse);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).appendSuper(super.toString()).append(
			"Gallery", this.gallery).toString(); //$NON-NLS-1$
	}
}
