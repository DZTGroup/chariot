package com.aperture.docx.core;

public interface BinarySaver {
	void setName(String name);
	void saveAsBinaryData(byte[] data);
}