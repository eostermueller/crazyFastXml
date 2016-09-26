package com.github.eostermueller.xpath;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
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

public class XPathSampler03 extends XPathSampler01 {
	private static DOMParserPool pool = new DOMParserPool();

	
	@Override
	protected String getXPathResults() {
        DocumentBuilder      domParser = null;
        Object result = null;
        StringBuilder sb = new StringBuilder();

        try {
            domParser = pool.getInstance().borrowDOMParser();
            InputStream xmlStream = XPathSampler01.class.getResourceAsStream(XPATH_INPUT_FILE);
            
			Document document = domParser.parse( xmlStream );
		
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
        } finally {
        	pool.returnDOMParser(domParser);
        }
        
        return sb.toString();
	}
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XPathSampler03 xmlParseSampler = new XPathSampler03();
			xmlParseSampler.setupTest(null);
	        String result = xmlParseSampler.getXPathResults();
			
	        System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
