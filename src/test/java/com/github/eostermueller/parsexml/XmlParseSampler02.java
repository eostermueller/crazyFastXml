package com.github.eostermueller.parsexml;

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

public class XmlParseSampler02 extends XmlParseSampler01 {
	private static SAXParserPool pool = new SAXParserPool();
	
	@Override
	protected void parseXml(String xmlInput) {
		this.bdHR7983 = new BigDecimal(0);
		this.bdHR8587  = new BigDecimal(0);
		this.bdHR8995 = new BigDecimal(0);
        SAXParser      saxParser = null;
        try {
            saxParser = pool.getInstance().borrowSAXParser();
            MyHandler handler   = new MyHandler();
            ByteArrayInputStream bais = new ByteArrayInputStream(xmlInput.getBytes());
            saxParser.parse( bais, handler);
            

        } catch (Throwable err) {
            throw new RuntimeException(err);
        } finally {
        	pool.returnSAXParser(saxParser);
        }
		
	}
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XmlParseSampler02 xmlParseSampler = new XmlParseSampler02();
			xmlParseSampler.setupTest(null);

	        StringBuilder sb = new StringBuilder();
	        xmlParseSampler.parseXml(xmlParseSampler.xml);
	        System.out.println(xmlParseSampler.formatResults());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
