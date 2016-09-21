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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.w3c.dom.Document;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.github.eostermueller.parse.SAXParserPool;

public class XmlDomParseSampler02 extends XmlDomParseSampler01 {
	private static DOMParserPool pool = new DOMParserPool();
	
	@Override
	protected void parseXml(String xmlInput) {
		this.bdHR7983 = new BigDecimal(0);
		this.bdHR8587  = new BigDecimal(0);
		this.bdHR8995 = new BigDecimal(0);
        DocumentBuilder      domParser = null;
        try {
            domParser = pool.getInstance().borrowDOMParser();
            InputStream xmlStream = XmlDomParseSampler01.class.getResourceAsStream(XmlDomParseSampler01.XML_INPUT_FILE);
            
			Document document = domParser.parse( xmlStream );
			processResults(document);
            

        } catch (Throwable err) {
            throw new RuntimeException(err);
        } finally {
        	pool.returnDOMParser(domParser);
        }
		
	}
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XmlDomParseSampler02 xmlDomParseSampler = new XmlDomParseSampler02();
			xmlDomParseSampler.setupTest(null);

	        StringBuilder sb = new StringBuilder();
	        xmlDomParseSampler.parseXml(xmlDomParseSampler.xml);
	        System.out.println(xmlDomParseSampler.formatResults());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
