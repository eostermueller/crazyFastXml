package com.github.eostermueller.util;

import javax.xml.transform.stream.StreamSource;

public interface TextFileLocator {
	public StreamSource getTextFileForThisFolder(String key);
}
