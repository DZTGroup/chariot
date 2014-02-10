package com.aperture.docx.core;

public interface BinaryLoader {
	long getId();
	String getName();
	
	// in case cache needed
	String getUpdateTag();
	
	byte[] loadAsBinaryData();
}
