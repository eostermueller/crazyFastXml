package com.github.eostermueller.validate;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URISyntaxException;

import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;

import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.xml.sax.SAXException;

import com.github.eostermueller.util.TextFileRepo;
import com.github.eostermueller.util.TextFileRepo.TextFileAndContents;
import com.github.eostermueller.util.TextFileRepos;

/**
 * How to create a SAXSource:  http://www.devx.com/DevX/Tip/32489
 * Pass InputSource and XMLReader to SAXSource ctor.
 * @author erikostermueller
 *
 */
public class XmlValidateSampler02  extends AbstractJavaSamplerClient {
	TextFileRepos repos = null;
	SchemaRepo schemaRepo = null;
	protected boolean ynUseCache = true;
	
	public XmlValidateSampler02() {
		this.ynUseCache = true;
	}
	
	private static final String PARM_XSD_FOLDER_ROOT =  "xsd.root.parm";
	public static final String XSD_ROOT = "/xsd.root";	
	/*
	 * (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#setupTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public void setupTest(JavaSamplerContext ctx) {
		File rootFolder_ = null;
		try {
			
			String rootFolder = null;
			if (ctx!=null) {
				rootFolder = ctx.getParameter(PARM_XSD_FOLDER_ROOT);
			}
			
			if (rootFolder!=null && rootFolder.trim().length()>0) {
				getLogger().info("Using xsd.root folder [" + rootFolder + "] from JMeter variable [" + this.PARM_XSD_FOLDER_ROOT + "]");
				rootFolder_ = new File(rootFolder);
				this.repos = new TextFileRepos(rootFolder_, TextFileRepos.OnePerFolder.XSD);
			} else {
				this.repos = new TextFileRepos(XSD_ROOT, TextFileRepos.OnePerFolder.XSD);
				getLogger().info("JMeter variable [" + PARM_XSD_FOLDER_ROOT + "] is ignored.");
			}
			getLogger().info("Process xsd and xml files from folder [" + this.repos.root.getAbsolutePath() + "] from classpath");
			
			SchemaRepo.SchemaFinder schemaFinder = new SchemaRepo.SchemaFinder() {
				@Override
				public File getSchema(String key) {
					return repos.getFile(key);
				}
			};
			this.schemaRepo = new SchemaRepo(schemaFinder, this.ynUseCache);
				
			
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			getLogger().error("Error reading xsd and xml files from disk. Stack trace:\n"+sw.toString(), e);
			getLogger().error("Message:\n"+e.getMessage());
		}
		
		
	}
	public SampleResult runTest(JavaSamplerContext ctx) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
    	
        SampleResult result = new SampleResult();
        result.sampleStart();
        int validationCount = 0;
        try {
        	
        	for(TextFileRepo repo : this.repos.getRepos()) {
        		for (TextFileAndContents xmlToBeValidated : repo.getXmlFiles() ) {
        			//debug("About to validate repo subfolder [" + repo.getName() + "] file [" + xmlToBeValidated.file.getName() + "] schema [" + repo.getOnePerFolder().file.getName() + "]"); 
        			validateXml(xmlToBeValidated.textFromFile, repo.getOnePerFolder().file.getName());
        			validationCount++;
        		}
        	}
        	result.setSuccessful(true);
    		result.setResponseCodeOK();
    	} catch(Exception e) {
			e.printStackTrace(pw);
			result.setSuccessful(false);
    	} finally {
    		result.setResponseData( "Validation count [" + validationCount + "]\n" + sw.toString(), "UTF-8");
    		result.sampleEnd();//time for validation of all xml
    	}
        	
		return result;
	}
	
	public static void main(String args[]) throws IOException, TransformerException, URISyntaxException {
		
		XmlValidateSampler02 sampler = new XmlValidateSampler02();
		sampler.setupTest(null);
		
		SampleResult sampleResult = sampler.runTest(null);
		System.out.println("Success [" + sampleResult.getResponseCode() + "]");
		System.out.println("Result [" + sampleResult.getResponseDataAsString() + "]");
		
	}
	void validateXml(String xmlInput, String schemaKey) throws SAXException, IOException {
		Schema schema = this.schemaRepo.get(schemaKey);
		Validator validator = schema.newValidator();

		StreamSource docSrc = new StreamSource(new StringReader(xmlInput));
		validator.validate(docSrc);		
	}
	public void debug(String string) {
		//System.out.println(string);
	}
}
