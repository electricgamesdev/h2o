package com.safik.hydrogen.engine;

import java.io.Serializable;

/**
 * Entity Object
 * 
 * @author safik
 *
 */
@SuppressWarnings("serial")
public class Entity implements Serializable{

	private String id;
	private String name;
	private String type;
	private String pattern;
	private String seperator;
	private boolean header;
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	public String getSeperator() {
		return seperator;
	}
	public void setSeperator(String seperator) {
		this.seperator = seperator;
	}
	public boolean isHeader() {
		return header;
	}
	public void setHeader(boolean header) {
		this.header = header;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (header ? 1231 : 1237);
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
		result = prime * result
				+ ((seperator == null) ? 0 : seperator.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Entity other = (Entity) obj;
		if (header != other.header)
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pattern == null) {
			if (other.pattern != null)
				return false;
		} else if (!pattern.equals(other.pattern))
			return false;
		if (seperator == null) {
			if (other.seperator != null)
				return false;
		} else if (!seperator.equals(other.seperator))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Entity [id=" + id + ", name=" + name + ", type=" + type
				+ ", pattern=" + pattern + ", seperator=" + seperator
				+ ", header=" + header + "]";
	}
	
	private boolean ok=false;
	public boolean isOk() {
		return ok;
	}
	public void setOk(boolean ok) {
		this.ok = ok;
	}
	
	private String eventId;
	public String getEventId() {
		return eventId;
	}
	
	public void setEventId(String eventId) {
		this.eventId = eventId;
	}
	
}
