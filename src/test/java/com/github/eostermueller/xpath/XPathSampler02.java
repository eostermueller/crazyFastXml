package com.github.eostermueller.xpath;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.github.eostermueller.parse.DOMParserPool;
import com.github.eostermueller.parse.XmlDomParseSampler01;

public class XPathSampler02 extends XPathSampler01 {
	
	@Override
	protected String getXPathResults() {
        StringBuilder sb = new StringBuilder();
		DocumentBuilderFactory builderFactory =
		        DocumentBuilderFactory.newInstance();

        try {
    		DocumentBuilder builder = builderFactory.newDocumentBuilder();
    		
            Object result = null;
            InputStream xmlStream = XPathSampler01.class.getResourceAsStream(XPATH_INPUT_FILE);
            
			Document document = builder.parse( xmlStream );
		
        	XPathExpression xpathExpr  = getExpression(XPATH_EXPR_01);
          // Evaluate the XPath expression against the input document
          result = xpathExpr.evaluate(document, XPathConstants.NODESET);
          //Format the result
          sb.append(  processResult01(result) );
          
      	xpathExpr  = getExpression(XPATH_EXPR_02);
        // Evaluate the XPath expression against the input document
        result = xpathExpr.evaluate(document, XPathConstants.NODESET);
        //Format the result
        sb.append(  processResult02(result) );
        } catch (Throwable err) {
            err.printStackTrace();
        }        
        return sb.toString();
	}
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XPathSampler02 xmlParseSampler = new XPathSampler02();
			xmlParseSampler.setupTest(null);
	        String result = xmlParseSampler.getXPathResults();
			
	        System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
