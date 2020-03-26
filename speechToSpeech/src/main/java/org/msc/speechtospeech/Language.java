package org.msc.speechtospeech;

public enum Language {
	DANISH("da"),
	SWEDISH("se"),
	ENGLISH("en"),
	NORWEGIAN("no"),
	GERMAN("de");
	
	private String languageCode;
	
	private Language(String languageCode) {
		this.languageCode = languageCode;
	}
	
	public String getLanguageCode() {
		return this.languageCode;
	}

}
