package com.github.eostermueller.parse;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class ValidatingSAXParserPool {
	private static GenericObjectPool<SAXParser> pool = null;
    private static class LazyHolder {
        private static final ValidatingSAXParserPool SINGLETON = new ValidatingSAXParserPool();
    }

    public static ValidatingSAXParserPool getInstance() {
        return LazyHolder.SINGLETON;
    }	
    public SAXParser borrowSAXParser() throws Exception {
    	return pool.borrowObject();
    }
    public void returnSAXParser(SAXParser parser) {
    	pool.returnObject(parser);
    }
	public ValidatingSAXParserPool() {
		GenericObjectPoolConfig config = new GenericObjectPoolConfig();
		
		/**
		 * set attributes on the config object before creating the pool
		 * https://commons.apache.org/proper/commons-pool/apidocs/org/apache/commons/pool2/impl/GenericObjectPoolConfig.html
		 */
		ValidatingSAXParserPool.pool = new GenericObjectPool<SAXParser>(new ValidatingSAXParserFactory(), config);
	}
	
}
class ValidatingSAXParserFactory extends BasePooledObjectFactory<SAXParser> {
    static SAXParserFactory factory = null;
    
	static {
		factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		factory.setValidating(true);
	}

	@Override
	public SAXParser create() throws Exception {
		SAXParser parser = factory.newSAXParser(); 
		parser.setProperty(
			    "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
			    "http://www.w3.org/2001/XMLSchema"
			);
		return parser;
	}

	@Override
	public PooledObject<SAXParser> wrap(SAXParser mySaxParser) {
		return new DefaultPooledObject<SAXParser>(mySaxParser);
	}
	@Override
	public void passivateObject(PooledObject<SAXParser> pooledObject) {
		pooledObject.getObject().reset();
	}
}
