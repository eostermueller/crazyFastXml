package com.github.eostermueller.validate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Stores compiled xml schemas (.xsd files).
 * During runtime, retrieves the pre-compiled Schemas so we don't waste CPU cycles re-compiling them.
 * 
 * The thread-safe parsed schemas are shared for all threads and don't have to be locked by a pool.
 * " a single Schema instance can be shared with many different parser instances even running in different threads."
 * ....according to https://jaxp.java.net/1.3/article/jaxp-1_3-article.html#Validate a SAXSource or DOMSource
 * @author erikostermueller
 *
 */
public class SchemaRepo {
	Map<String,Schema> schemas = new ConcurrentHashMap<String,Schema>();
	private SchemaFinder schemaFinder;
	private boolean ynUseCache;

	SchemaRepo(SchemaFinder schemaFinder, boolean ynUseCache) {
		this.schemaFinder = schemaFinder;
		this.ynUseCache = ynUseCache;
	}

    /**
     * Parse given W3C XML Schema 'File' (probably an .xsd) and store so it can later be used for validation.
     * @param key
     * @param schemaFile
     * @throws SAXException
     */
	private Schema put(String key, File schemaFile) throws SAXException {
		//create a SchemaFactory capable of understanding W3C XML Schemas (WXS)
		SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		//Set the error handler to receive any error during Schema Compilation
		factory.setErrorHandler(new MyErrorHandler());
		//set the resource resolver to customize resource resolution
		factory.setResourceResolver( new MyLSResourceResolver());
		// load a WXS schema, represented by a Schema instance 
		Schema schema = factory.newSchema(new StreamSource(schemaFile));
		
		/**
		 * This boolean provides the two sides of a performance comparison.
		 * If ynUseCache==false, the .xsd is compiled for every request.
		 * If ynUseCache==true, the .xsd is compiled once and subsequently retrieved from the cahce.
		 * 
		 */
		if (this.ynUseCache)
			this.schemas.put(key, schema);
		return schema;
	}
	
	public Schema get(String key) throws SAXException {
		Schema schema = this.schemas.get(key);
		if (schema==null) {
			File schemaFile = this.schemaFinder.getSchema(key);
			schema = put(key,schemaFile);
		}
		return schema;
	}
	public static interface SchemaFinder {
		File getSchema(String key);
	}
	/**
	 * type=[http://www.w3.org/2001/XMLSchema] namespaceURI=[http://projecthdata.org/hdata/schemas/2009/06/core] publicId[null] systemId[core_data_types.xsd] baseURI[file:/Users/erikostermueller/Documents/src/jsource/crazyFastXml/target/test-classes/xsd.root/01/allergy.xsd]
	 * @author erikostermueller
	 *
	 */
	public class MyLSResourceResolver implements LSResourceResolver {

		
		public LSInput resolveResource(String type, String namespaceURI,
				String publicId, String systemId, String baseURI) {
			//System.out.println("type=[" + type + "] namespaceURI=[" + namespaceURI + "] publicId[" + publicId + "] systemId[" + systemId + "] baseURI[" + baseURI + "]");
			File schema = schemaFinder.getSchema( systemId );
			MyLSInput myLSInput = new MyLSInput(schema);
			return myLSInput;
		}
	}
	private static class MyLSInput implements LSInput {
		String systemId = null;
		String publicId = null;
		File file = null;
		String fileData = null;
		private String baseURI;
		public MyLSInput(File schema) {
			file = schema;
		}

		@Override
		public Reader getCharacterStream() {
			FileReader fr = null; 
			try {
				fr = new FileReader(this.file);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			return fr;
		}

		@Override
		public void setCharacterStream(Reader characterStream) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public InputStream getByteStream() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setByteStream(InputStream byteStream) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getStringData() {
			
			try {
				this.fileData = new String(Files.readAllBytes(Paths.get(this.file.getAbsolutePath())), StandardCharsets.UTF_8);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return this.fileData;
		}

		@Override
		public void setStringData(String stringData) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public String getSystemId() {
			return this.systemId;
		}

		@Override
		public void setSystemId(String systemId) {
			this.systemId = systemId;
		}

		@Override
		public String getPublicId() {
			return this.publicId;
		}

		@Override
		public void setPublicId(String publicId) {
			this.publicId  = publicId;
		}

		@Override
		public String getBaseURI() {
			return baseURI;
		}

		@Override
		public void setBaseURI(String baseURI) {
			this.baseURI = baseURI;
			
		}

		@Override
		public String getEncoding() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setEncoding(String encoding) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean getCertifiedText() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public void setCertifiedText(boolean certifiedText) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	
}
class MyErrorHandler implements ErrorHandler {

	public void warning(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void error(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		// TODO Auto-generated method stub
		
	}
	
	
}
