package com.github.eostermueller.xslt;

import javax.xml.transform.stream.StreamSource;

public interface XslLocator {
	public StreamSource getXsl(String key);
}
