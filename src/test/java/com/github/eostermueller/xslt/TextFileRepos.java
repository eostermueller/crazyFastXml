package com.github.eostermueller.xslt;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;

/**
 * Simple test-only class to read txt content xsl and xml files from the file system,
 * whose file structure must be organized according to these rules:
 * <ul>
 * 		<li>Rule 1: Each xsl file must reside in a folder with one or more XML files that it transforms</li>
 * 		<li>Rule 2: Only one xsl file per folder.</li>
 * 		<li>Rule 3: One or more folders structured like list must be arranged as sibling folders under a parent.</li>
 * 		<li>Rule 4: That parent folder is referred to as the root folder, which must be passed into the the ctor of this class.</li>
 * 		<li>Rule 5: The root folder can have no immediate child files, just child folders.
 * 		
 *  Sample file structure, where "xslt.xml.root" is considered the "root folder"
 *  <pre>
xslt.xml.root/
├── 01
│   ├── personnel.xml
│   └── simple.xsl
├── 02
│   ├── book.xml
│   └── to-html.xsl
├── 03
│   ├── sales.xml
│   └── to-html.xsl
├── 04
│   ├── sales.xml
│   └── to-svg.xsl
├── 05
│   ├── foo.xml
│   └── foo.xsl
└── 06
    ├── birds.xml
    └── birds.xsl
    </pre>
 * It is expected that your system will have its own ways/structures for storing/retrieving .xsl files,
 * so it should implement XslLocator.
 * 
 *
 * @author erikostermueller
 *
 */
public class TextFileRepos implements XslLocator {
	List<TextFileRepo> transformationRepos = new ArrayList<TextFileRepo>();
	File root = null;
	public TextFileRepos(File root) throws IOException {
		info("Root folder is [" + root.getAbsolutePath() + "]");
		this.root = root;
		if (!root.isDirectory()) {
			throw new RuntimeException("The given name [" + root.getAbsolutePath() + "] must be a directory.");
		}
		
		for(String fileName : root.list()) {
			this.info("Processing folder [" + fileName + "].");
			File f = new File(root, fileName);
			if (f.isDirectory()) {
				TextFileRepo t = new TextFileRepo(f);
				this.transformationRepos.add(t);
			} else {
				info("Ignoring [" + f.getAbsolutePath() + "].  See Rule 1 in the java doc for Transformations.java");
			}
		}
	}
	public String dumpToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Root folder is [" + root.getAbsolutePath()  + "]\n");
		for(TextFileRepo repo : this.transformationRepos) {
			sb.append( repo.dumpToString() );
		}
		return sb.toString();
	}
	private void info(String msg) {
		System.out.println(msg);
	}	
	public static void main(String args[]) throws IOException {
		File rootFolder = new File(args[0]);
		TextFileRepos repos = new TextFileRepos(rootFolder);
		System.out.println("Loaded xsl and xml repos:\n");
		System.out.println(repos.dumpToString());
	}
	
	/**
	 * Not designed for speed -- only expecting to be used at system startup time.
	 */
	public TextFileRepo getTextFileRepo(String repoNameCriteria) {
		TextFileRepo result  = null;
		for(TextFileRepo r : this.transformationRepos) {
			if(r.getName().equals(repoNameCriteria))
				result = r;
		}
		return result;
	}
	private String getRepoNames() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (TextFileRepo r : this.transformationRepos) {
			if (count++>0)
				sb.append(",");
			sb.append(r.getName());
		}
		return sb.toString();
	}
	/**
	 * Not designed for speed -- only expecting to be used at system startup time.
	 */
	public StreamSource getXsl(String key) {
		TextFileRepo r = this.getTextFileRepo(key);
		if (r==null) {
			throw new RuntimeException("Did not find repo key [" + key + "] in this list of repo names [" + getRepoNames() + "]");
		}
		return new StreamSource(new StringReader(r.getXsl().textFromFile));
	}
}
