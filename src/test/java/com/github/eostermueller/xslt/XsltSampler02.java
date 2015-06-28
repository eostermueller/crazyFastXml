package com.github.eostermueller.xslt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.NoSuchElementException;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.xml.sax.InputSource;

import com.github.eostermueller.xslt.TextFileRepo.TextFileAndContents;

public class XsltSampler02 extends AbstractJavaSamplerClient {
	
	/*
	 * Used to delimit parts of the key that will store the Transformer.
	 */
	private static final String PARM_XSLT_AND_FOLDER_ROOT = "xslt.and.xml.folder.root";
	TextFileRepos repos = null;
	static KeyedPooledObjectFactory keyedPooledObjectFactory = null;
	static KeyedObjectPool<String,Transformer> keyedPool = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#setupTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public void setupTest(JavaSamplerContext ctx) {

		String rootFolder = ctx.getParameter(PARM_XSLT_AND_FOLDER_ROOT);
		File rootFolder_ = new File(rootFolder);
		try {
			this.repos = new TextFileRepos(rootFolder_);
	        for(TextFileRepo repo : repos.transformationRepos){

	        	for(TextFileAndContents xml : repo.getXmlFiles()) {
	        	}
	        }
	        //Factory will get .xsl files from our home-baked test repository of xsl & xml files.
	        this.keyedPooledObjectFactory = new KeyedTransformerFactory(this.repos);
	        
	        //To configure min/max and other pool parameters, 
	        //create GenericKeyedObjectPoolConfig and pass as 2nd parm here:
	        this.keyedPool = new GenericKeyedObjectPool<String,Transformer>(this.keyedPooledObjectFactory);
	        
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			getLogger().error("Error reading xsl and xml files from disk. Stack trace:\n"+sw.toString(), e);
		}
	}
    /* set up default arguments for the JMeter GUI
     * (non-Javadoc)
     * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(PARM_XSLT_AND_FOLDER_ROOT, null );
        return defaultParameters;
    }
	public SampleResult runTest(JavaSamplerContext ctx) {
    	
        SampleResult result = new SampleResult();
        result.sampleStart(); // start stopwatch    	
        StringBuilder sb = new StringBuilder();
        try {
			performXslt(this.repos, sb);
			result.setResponseData(sb.toString(), "UTF-8");
			result.setSuccessful(true);
			result.setResponseCodeOK();
			result.sampleEnd();//time for all transformations
		} catch (TransformerException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			result.setResponseData(sw.toString(), "UTF-8");
			result.setSuccessful(false);
		}
		return result;
	}
	/**
	 * Serially, perform one tranformation for each .xml file in our little test repository of xml & xsl files.
	 * @param repos
	 * @param sb
	 * @throws TransformerException
	 */
	private static void performXslt(TextFileRepos repos, StringBuilder sb) throws TransformerException {
		
        for(TextFileRepo repo : repos.transformationRepos){

        	for(TextFileAndContents xml : repo.getXmlFiles()) {
                SAXSource saxSource = new SAXSource(new InputSource( new StringReader(xml.textFromFile) ));
                StringWriter writer = new StringWriter();
                
                Transformer transformer = null;
				try {
					transformer = keyedPool.borrowObject(repo.getName());
	    			transformer.transform(saxSource, new StreamResult(writer));
	    			sb.append("#Repo:" + repo.directory.getName() + " XMl file:" + xml.file.getName() + " XSL file: " + repo.getXsl().file.getName() + "\n");
	    			sb.append(writer.toString());
	    			sb.append("\n");
				} catch (Exception e) {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					e.printStackTrace(pw);
					sb.append(sw.toString());
				} finally {
					try {
						keyedPool.returnObject(repo.getName(), transformer);
					} catch (Exception e) {
						StringWriter sw = new StringWriter();
						PrintWriter pw = new PrintWriter(sw);
						e.printStackTrace(pw);
						sb.append(sw.toString());
					}
				}
        	}
        }
	}
	public static void main(String args[]) throws IOException, TransformerException {
		try {
			File root = new File(args[0]);
			TextFileRepos repos = new TextFileRepos(root);
	        StringBuilder sb = new StringBuilder();
	        performXslt(repos, sb);
	        System.out.println(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
