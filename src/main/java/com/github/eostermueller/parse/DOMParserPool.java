package com.github.eostermueller.parse;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class DOMParserPool {
	private static GenericObjectPool<DocumentBuilder> pool = null;
    private static class LazyHolder {
        private static final DOMParserPool SINGLETON = new DOMParserPool();
    }

    public static DOMParserPool getInstance() {
        return LazyHolder.SINGLETON;
    }	
    public DocumentBuilder borrowDOMParser() throws Exception {
    	return pool.borrowObject();
    }
    public void returnDOMParser(DocumentBuilder parser) {
    	pool.returnObject(parser);
    }
	public DOMParserPool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		
		/**
		 * set attributes on the config object before creating the pool
		 * https://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/impl/GenericObjectPoolConfig.html
		 */
		this.pool = new GenericObjectPool<DocumentBuilder>(new DomParserFactory(), config);
	}
	
}
class DomParserFactory extends BasePooledObjectFactory<DocumentBuilder> {
    static final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();

	@Override
	public DocumentBuilder create() throws Exception {
		return domFactory.newDocumentBuilder();
	}

	@Override
	public PooledObject<DocumentBuilder> wrap(DocumentBuilder myDomParser) {
		return new DefaultPooledObject<DocumentBuilder>(myDomParser);
	}
	@Override
	public void passivateObject(PooledObject<DocumentBuilder> pooledObject) {
		pooledObject.getObject().reset();
	}


}
