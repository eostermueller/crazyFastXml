package com.github.eostermueller.parse;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Scanner;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlParseSampler01 extends AbstractJavaSamplerClient {
	BigDecimal bdHR7983 = null;
	BigDecimal bdHR8587  = null;
	BigDecimal bdHR8995 = null;
	/*
	 * HR = Homicide Rate per 100k people
	 */
	//private static final String XML_INPUT_FILE = "/NCORV.xml";
	private static final String XML_INPUT_FILE = "/xml.root/NCORV.xml";
	String xml = null;
	/*
	 * (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#setupTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public void setupTest(JavaSamplerContext ctx) {
		//com.github.eostermueller.util.Util.initSlowXML();
		InputStream xmlStream = XmlParseSampler01.class.getResourceAsStream(XML_INPUT_FILE);
		this.xml = new Scanner(xmlStream,"UTF-8").useDelimiter("\\A").next();
		System.out.println("Found [" + xml.length() + "] bytes in the xml input file [" + XML_INPUT_FILE + "]");
	}
	public String formatResults() {
		StringBuilder sb = new StringBuilder();
		sb.append("Between 1979 and 1983, there were [" + this.bdHR7983 + "] homicides per 100k people in the data file\n");
		sb.append("Between 1985 and 1987, there were [" + this.bdHR8587 + "] homicides per 100k people in the data file\n");
		sb.append("Between 1989 and 1995, there were [" + this.bdHR8995 + "] homicides per 100k people in the data file\n");
		return sb.toString();
	}
	public SampleResult runTest(JavaSamplerContext ctx) {
    	
        SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch    	
        try {
			parseXml(xml);
			result.setResponseData(formatResults(), "UTF-8");
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.sampleEnd();//time for all transformations
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			result.setResponseData(sw.toString(), "UTF-8");
			result.setSuccessful(false);
		}
		return result;
	}
	
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XmlParseSampler01 xmlParseSampler = new XmlParseSampler01();
			xmlParseSampler.setupTest(null);

	        StringBuilder sb = new StringBuilder();
	        xmlParseSampler.parseXml(xmlParseSampler.xml);
	        System.out.println(xmlParseSampler.formatResults());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected void parseXml(String xmlInput) {
		this.bdHR7983 = new BigDecimal(0);
		this.bdHR8587  = new BigDecimal(0);
		this.bdHR8995 = new BigDecimal(0);
		
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {

            SAXParser      saxParser = factory.newSAXParser();
            MyHandler handler   = new MyHandler();
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlInput.getBytes());
            saxParser.parse( bais, handler);

        } catch (Throwable err) {
            err.printStackTrace ();
        }		
		
	}
	private enum CountType {
		HR7983, //between 1979 and 1983
		HR8587, //between 1985 and 1987 
		HR8995,  //between 1989 and 1995
		OTHER
	}
	class MyCountHandler extends DefaultHandler {
		
	}
	class MyHandler extends DefaultHandler {
		
		CountType current = null;
		@Override
	    public void startElement (String uri, String localName,
                String qName, Attributes attributes) throws SAXException {
			
			if (qName.equals(CountType.HR7983.toString())) {
				this.current = CountType.HR7983;
			} else if (qName.equals(CountType.HR8587.toString())) {
				this.current = CountType.HR8587;
			} else if (qName.equals(CountType.HR8995.toString())) {
				this.current = CountType.HR8995;
			} else {
				this.current = CountType.OTHER;
			}
			//debug("current [" + this.current + "] uri[" + uri + "] localname[" + localName + "] qName[" + qName + "]");
		}	
		public void characters(char ch[], int start, int length) throws SAXException {
			 
			String data = new String(ch, start, length);
			BigDecimal bd = null;
			if (this.current != null) {
				switch(this.current) {
				case HR7983:
					bd = new BigDecimal(data);
					bdHR7983 = bdHR7983.add(bd);
					break;
				case HR8587:
					bd = new BigDecimal(data);
					bdHR8587 = bdHR8587.add(bd);
					break;
				case HR8995:
					bd = new BigDecimal(data);
					bdHR8995 = bdHR8995.add(bd);
					break;
				case OTHER:
					break;
				default:
					break;
				}
			}
			this.current = CountType.OTHER;
		}
	}
	public void debug(String string) {
		//System.out.println(string);
	}
}
