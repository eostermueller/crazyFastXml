package com.github.eostermueller.xpath;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import com.github.eostermueller.util.TextFileLocator;

public class KeyedXPathFactory extends BaseKeyedPooledObjectFactory<String, XPathExpression> {

    @Override
    public XPathExpression create(String xpathExpression) throws TransformerConfigurationException {
    	
        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        
      // compile the XPath expression
      XPathExpression xpathExpr = null;
	try {
		xpathExpr = xpath.compile(xpathExpression);
	} catch (XPathExpressionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
      return xpathExpr;
    }

    @Override
    public PooledObject<XPathExpression> wrap(XPathExpression t) {
        return new DefaultPooledObject<XPathExpression>(t);
    }

    @Override
    public void passivateObject(String key, PooledObject<XPathExpression> pooledObject) {
    }

}
