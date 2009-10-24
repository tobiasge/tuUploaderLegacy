package com.teamulm.uploadsystem.data;

import java.io.Serializable;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Location implements Serializable, Comparable<Location> {

	private static final long serialVersionUID = -4083430320962596602L;

	private int id = -1;

	private String name = null;

	public Location(int id, String name) {
		super();
		this.id = id;
		this.name = name;
	}

	public int compareTo(Location rhs) {
		Location lhs = this;
		return new CompareToBuilder().append(lhs.name, rhs.name).toComparison();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Location) {
			Location rhs = (Location) obj;
			Location lhs = (Location) this;
			return new EqualsBuilder().append(lhs.id, rhs.id).append(lhs.name, rhs.name).isEquals();
		}
		return false;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(15, 17).append(this.id).append(this.name).toHashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("Id", this.id).append("Name", //$NON-NLS-1$ //$NON-NLS-2$
			this.name).toString();
	}
}
