package com.aperture.docx.templating.dependency;

import java.util.Map;

public interface Statement{
	boolean apply(Map<String, String> answers);
}