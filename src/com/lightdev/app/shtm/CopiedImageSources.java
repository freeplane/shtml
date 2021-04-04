package com.lightdev.app.shtm;

import java.util.regex.Pattern;

public enum CopiedImageSources {
	ANY, 
	ANY_ABSOLUTE_URL(Pattern.compile("^\\w{2,}:/", Pattern.CASE_INSENSITIVE)), 
	ANY_HTTP_URL(Pattern.compile("^https?:/", Pattern.CASE_INSENSITIVE)), 
	NONE;
	private final Pattern pattern;
	

	private CopiedImageSources() {
		this(null);
	}
	private CopiedImageSources(Pattern pattern) {
		this.pattern = pattern;
	}
	boolean includes(String source) {
		if (source == null)
			return false;
		switch (this) {
		case ANY:
			return true;
		case NONE:
			return false;
		default:
			return pattern.matcher(source).find();
		}
	}

}