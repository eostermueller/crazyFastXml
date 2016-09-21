package com.github.eostermueller.parse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XmlDomParseSampler01 extends AbstractJavaSamplerClient {
	BigDecimal bdHR7983 = null;
	BigDecimal bdHR8587  = null;
	BigDecimal bdHR8995 = null;
	/*
	 * HR = Homicide Rate per 100k people
	 */
	//private static final String XML_INPUT_FILE = "/NCORV.xml";
	protected static final String XML_INPUT_FILE = "/xml.root/NCORV.xml";
	String xml = null;
	/*
	 * (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#setupTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public void setupTest(JavaSamplerContext ctx) {
		//com.github.eostermueller.util.Util.initSlowXML();
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
			XmlDomParseSampler01 xmlParseSampler = new XmlDomParseSampler01();
			xmlParseSampler.setupTest(null);

	        StringBuilder sb = new StringBuilder();
	        xmlParseSampler.parseXml(xmlParseSampler.xml);
	        System.out.println(xmlParseSampler.formatResults());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	protected void parseXml(String xmlInput) {
//		this.bdHR7983 = new BigDecimal(0);
//		this.bdHR8587  = new BigDecimal(0);
//		this.bdHR8995 = new BigDecimal(0);
		
		try {
			DocumentBuilderFactory builderFactory =
			        DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			InputStream xmlStream = XmlDomParseSampler01.class.getResourceAsStream(XML_INPUT_FILE);
			//this.xml = new Scanner(xmlStream,"UTF-8").useDelimiter("\\A").next();
			//System.out.println("Found [" + xml.length() + "] bytes in the xml input file [" + XML_INPUT_FILE + "]");
			
			Document document = builder.parse( xmlStream );
			processResults(document);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
		
	}
	protected void processResults(Document document) {
		NodeList nodeList = document.getElementsByTagName("HR7983");
		this.bdHR7983 = sum(nodeList);
		nodeList = document.getElementsByTagName("HR8587");
		this.bdHR8587 = sum(nodeList);
		nodeList = document.getElementsByTagName("HR8995");
		this.bdHR8995 = sum(nodeList);
		
	}
	private BigDecimal sum(NodeList nodeList) {
		BigDecimal bd = new BigDecimal(0);
		for(int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n.getNodeType()==Node.ELEMENT_NODE) {
				Node text = n.getFirstChild();
				if (text.getNodeType()==Node.TEXT_NODE) {
					BigDecimal tmpBd = new BigDecimal( text.getTextContent() );
					bd = bd.add(tmpBd);
				}
			}
		}
		return bd;
	}
	private enum CountType {
		HR7983, //between 1979 and 1983
		HR8587, //between 1985 and 1987 
		HR8995,  //between 1989 and 1995
		OTHER
	}
	public void debug(String string) {
		//System.out.println(string);
	}
}
