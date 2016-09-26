package com.github.eostermueller.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.github.eostermueller.parse.DOMParserPool;
import com.github.eostermueller.parse.XmlDomParseSampler01;

public class XPathSampler04 extends XPathSampler01 {
	private static KeyedObjectPool<String,XPathExpression> xpathPool = 
			new GenericKeyedObjectPool<String,XPathExpression>(new KeyedXPathFactory());

	private static DOMParserPool domParserPool = new DOMParserPool();

	
	@Override
	protected String getXPathResults() {
        DocumentBuilder      domParser = null;
        Object result = null;
        StringBuilder sb = new StringBuilder();

        try {
            domParser = domParserPool.getInstance().borrowDOMParser();
            InputStream xmlStream = XPathSampler01.class.getResourceAsStream(XPATH_INPUT_FILE);
            
			Document document = domParser.parse( xmlStream );
		
        	XPathExpression xpathExpr  = null;
			try {
				xpathExpr = this.xpathPool.borrowObject(XPATH_EXPR_01);
		          // Evaluate the XPath expression against the input document
		          result = xpathExpr.evaluate(document, XPathConstants.NODESET);
		          //Format the result
		          sb.append(  processResult01(result) );
			} finally {
				xpathPool.returnObject(XPATH_EXPR_01, xpathExpr);
			}

			try {
				xpathExpr = this.xpathPool.borrowObject(XPATH_EXPR_02);
		          // Evaluate the XPath expression against the input document
		          result = xpathExpr.evaluate(document, XPathConstants.NODESET);
		          //Format the result
		          sb.append(  processResult02(result) );
			} finally {
				xpathPool.returnObject(XPATH_EXPR_02, xpathExpr);
			}
        } catch (Throwable err) {
            err.printStackTrace();
        } finally {
        	domParserPool.returnDOMParser(domParser);
        }
        
        return sb.toString();
	}
	
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XPathSampler04 xmlParseSampler = new XPathSampler04();
			xmlParseSampler.setupTest(null);
	        String result = xmlParseSampler.getXPathResults();
			
	        System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
