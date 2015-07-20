package com.github.eostermueller.xslt;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient;
import org.apache.jmeter.protocol.java.sampler.JavaSamplerContext;
import org.apache.jmeter.samplers.SampleResult;
import org.xml.sax.InputSource;

import com.github.eostermueller.util.TextFileRepo;
import com.github.eostermueller.util.TextFileRepos;
import com.github.eostermueller.util.TextFileRepo.TextFileAndContents;
import com.github.eostermueller.util.TextFileRepos.OnePerFolder;

public class XsltSampler01 extends AbstractJavaSamplerClient {
	private static final String PARM_XSLT_AND_FOLDER_ROOT = "xsl.root";
	public static final String XSL_ROOT = "/xsl.root";
	TextFileRepos repos = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#setupTest(org.apache.jmeter.protocol.java.sampler.JavaSamplerContext)
	 */
	@Override
	public void setupTest(JavaSamplerContext ctx) {
		File rootFolder_ = null;
		try {
			
			String rootFolder = ctx.getParameter(PARM_XSLT_AND_FOLDER_ROOT);
			if (rootFolder!=null && rootFolder.trim().length()>0) {
				getLogger().info("Using xsl.root folder [" + rootFolder + "] from JMeter variable [" + PARM_XSLT_AND_FOLDER_ROOT + "]");
				rootFolder_ = new File(rootFolder);
				this.repos = new TextFileRepos(rootFolder_, TextFileRepos.OnePerFolder.XSL);
			} else {
				this.repos = new TextFileRepos(XsltSampler01.XSL_ROOT, TextFileRepos.OnePerFolder.XSL);
				getLogger().info("JMeter variable [" + PARM_XSLT_AND_FOLDER_ROOT + "] is ignored.");
			}
			getLogger().info("Process xsl and xml files from folder [" + this.repos.root.getAbsolutePath() + "] from classpath");
			
		} catch (IOException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			getLogger().error("Error reading xsl and xml files from disk. Stack trace:\n"+sw.toString(), e);
			getLogger().error("Message:\n"+e.getMessage());
		} catch (URISyntaxException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			getLogger().error("Error reading xsl and xml files from disk. Stack trace:\n"+sw.toString(), e);
			getLogger().error("Message:\n"+e.getMessage());
		}
	}
    /* set up default arguments for the JMeter GUI
     * (non-Javadoc)
     * @see org.apache.jmeter.protocol.java.sampler.AbstractJavaSamplerClient#getDefaultParameters()
     */
    @Override
    public Arguments getDefaultParameters() {
        Arguments defaultParameters = new Arguments();
        defaultParameters.addArgument(this.XSL_ROOT, null );
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
	public File getFolderInsideZip(URI uri) throws IOException, URISyntaxException {
		Map<String, String> env = new HashMap<String, String>(); 
		env.put("create", "true");
		String pathParts[] = uri.toString().split("!");
		
		FileSystem zipfs = FileSystems.newFileSystem(new URI(pathParts[0]), env);
		Path myFolderPath = Paths.get(pathParts[1]);
		return myFolderPath.toFile();
		
	}
	
	private static void performXslt(TextFileRepos repos, StringBuilder sb) throws TransformerException {
		
        for(TextFileRepo repo : repos.repos){

        	for(TextFileAndContents xml : repo.getXmlFiles()) {
                SAXSource saxSource = new SAXSource(new InputSource( new StringReader(xml.textFromFile) ));
                StringWriter writer = new StringWriter();
                TransformerFactory factory = SAXTransformerFactory.newInstance();
                Transformer transformer;
    			transformer = factory.newTransformer(new StreamSource(new StringReader(repo.getOnePerFolder().textFromFile)));
    			transformer.transform(saxSource, new StreamResult(writer));
    			sb.append("#Repo:" + repo.directory.getName() + " XMl file:" + xml.file.getName() + " XSL file: " + repo.getOnePerFolder().file.getName() + "\n");
    			sb.append(writer.toString());
    			sb.append("\n");
        	}
        	
        }
		
	}
	public static void main(String args[]) throws IOException, TransformerException, URISyntaxException {
		File root = null;
		try {
			XsltSampler01 sampler = new XsltSampler01();
			TextFileRepos repos = null;
			if (args.length == 1 && args[0] != null) {
				root = new File(args[0]);
				repos = new TextFileRepos(root,OnePerFolder.XSL);
			} else {
				repos = new TextFileRepos(XSL_ROOT,OnePerFolder.XSL);
			}
			
	        StringBuilder sb = new StringBuilder();
	        performXslt(repos, sb);
	        System.out.println(sb.toString());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
