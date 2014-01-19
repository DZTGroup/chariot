package com.aperture.docx.core;

public interface BinaryLoader {
	long getId();
	String getName();
	byte[] loadAsBinaryData();
}
