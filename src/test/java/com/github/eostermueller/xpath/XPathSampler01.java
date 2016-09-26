package com.github.eostermueller.xpath;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XPathSampler01 extends AbstractJavaSamplerClient {
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			XPathSampler01 xmlParseSampler = new XPathSampler01();
			xmlParseSampler.setupTest(null);

	        String result = xmlParseSampler.getXPathResults();
			
	        System.out.println(result);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/*
	 * HR = Homicide Rate per 100k people
	 */
	protected static final String XPATH_INPUT_FILE = "/xpath.root/js-bach-works.xml";
	protected static final String XPATH_EXPR_01 = "/Compositions/Work[Key='C major']";
	protected static final String XPATH_EXPR_02 = "/Compositions/Work/BC[starts-with(.,'G2')]";
	String xml = null;
	/*
	 * (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#setupTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public void setupTest(JavaSamplerContext ctx) {
	}
	public InputSource getInputSource() {
		InputStream xmlStream = XPathSampler01.class.getResourceAsStream(XPATH_INPUT_FILE);
		InputSource xml = new InputSource(xmlStream);
		return xml;
	}
	public SampleResult runTest(JavaSamplerContext ctx) {
  	
        SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch    	
		String textResults = null;
        try {
			textResults = getXPathResults();
			result.setResponseData(textResults, "UTF-8");
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
	
	protected String getXPathResults() {

        Object result = null;
        StringBuilder sb = new StringBuilder();
        String rc = null;
        try {

        	XPathExpression xpathExpr  = getExpression(XPATH_EXPR_01);
          
			// Evaluate the XPath expression against the input document
			result = xpathExpr.evaluate(getInputSource(), XPathConstants.NODESET);
			  
			//Format the result
			sb.append( processResult01(result) );
          
        	xpathExpr  = getExpression(XPATH_EXPR_02);
            
			// Evaluate the XPath expression against the input document
        	// Unfortunately, I don't know how to reset the inputstream, 
        	// so we'll re-read the entire thing here.
			result = xpathExpr.evaluate(getInputSource(), XPathConstants.NODESET);
			  
			//Format the result
			sb.append( processResult02(result) );
			
        } catch (Throwable err) {
            err.printStackTrace();
        }		
        
        return sb.toString();
	}
        
	protected Object processResult02(Object result) {
		StringBuilder rc = new StringBuilder();
        NodeList bcList = (NodeList)result;
        for (int i = 0; i < bcList.getLength(); i++) {
      	  //Get the <BC> items
      	  Node bcNode = bcList.item(i);
      	  
      	  rc.append(bcNode.getTextContent()).append("\n");
        }

		return rc.toString();
	}
	protected String processResult01(Object result) {
		StringBuilder rc = new StringBuilder();
        NodeList workList = (NodeList)result;
        for (int i = 0; i < workList.getLength(); i++) {
      	  //Get the <Work>
      	  //Example:  <Work><BWV>19</BWV><BC>A180</BC><Title>Es erhub sich ein Streit</Title><Key>C major</Key><Date>1726</Date><Genre>Sacred cantatas</Genre><Scoring>For 3 voices, mixed chorus, orchestra</Scoring><Notes></Notes></Work>
      	  Node workNode = workList.item(i);
      	  
      	  //Example: <BWV>19</BWV>
      	  Node bwvNode = workNode.getFirstChild();
      	  rc.append(bwvNode.getTextContent()).append("\n");
      	  //System.out.println("First child [" + bwvNode.getNodeName() + "] value ["  + bwvNode.getTextContent() + "]");
      	  
        }

		return rc.toString();
	}
	protected XPathExpression getExpression(String expr) {

        // Create a new XPath
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        
      // compile the XPath expression
      XPathExpression xpathExpr = null;
	try {
		xpathExpr = xpath.compile(expr);
	} catch (XPathExpressionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      return xpathExpr;
	}
	public void debug(String string) {
		//System.out.println(string);
	}
}
