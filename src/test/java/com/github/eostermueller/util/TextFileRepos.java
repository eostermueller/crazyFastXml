package com.github.eostermueller.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

/**
 * Need to recode this to pull from either jar or file the file system, as detailed here:
 * http://stackoverflow.com/questions/11012819/how-can-i-get-a-resource-folder-from-inside-my-jar-file
 * 
 * 
 * Simple test-only class to read txt content xsl and xml files from the file system,
 * whose file structure must be organized according to these rules:
 * <ul>
 * 		<li>Rule 1: Each xsl file must reside in a folder with one or more XML files that it transforms</li>
 * 		<li>Rule 2: Only one xsl file per folder.</li>
 * 		<li>Rule 3: One or more folders structured like this must be arranged as sibling folders under a parent.</li>
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
public class TextFileRepos implements TextFileLocator {
	public static FileFilter XSD_FILE_FILTER = new XsdFile();
	public static FileFilter XSL_FILE_FILTER = new XslFile();
	public List<TextFileRepo> repos = new ArrayList<TextFileRepo>();
	public File root = null;
	private OnePerFolder onePerFolder;
	private List<TextFileRepo.TextFileAndContents> unparsedRootXsdFiles = new ArrayList<TextFileRepo.TextFileAndContents>();
	
	
	public TextFileRepos(String folderInClasspath, OnePerFolder onePerFolder) throws IOException, URISyntaxException {
		
		this(
				getDefaultRootFolder(folderInClasspath),
				onePerFolder);
	}
	/**
	 * 
	 * @param root
	 * @param onePerFolder -- Identifies either a .xsd or a .xsl file.  This code expects either
	 * @throws IOException
	 */
	public TextFileRepos(File root, OnePerFolder onePerFolder) throws IOException {
		info("Root folder is [" + root.getAbsolutePath() + "]");
		this.root = root;
		if (!root.isDirectory()) {
			throw new RuntimeException("The given name [" + root.getAbsolutePath() + "] must be a directory.");
		}
		this.onePerFolder = onePerFolder;
		loadChildRepositories();
		if (this.onePerFolder==OnePerFolder.XSD) {
			loadRootXsdFiles();
		}
	}
	private void loadRootXsdFiles() throws IOException {
		FileFilter xsdFilter = new FileFilter() {
			public boolean accept(File pathname) {
				boolean rc = false;
				if (pathname.getName().endsWith(".xsd"))
					rc = true;
				return rc;
			}
		};
		File[] files = this.root.listFiles( xsdFilter );
		for(File f : files) {
			if (f.isFile()) {
				TextFileRepo.TextFileAndContents t = new TextFileRepo.TextFileAndContents(f);
				this.unparsedRootXsdFiles.add(t);
			}
		}
	}
	private void loadChildRepositories() throws IOException {
		for(String fileName : root.list()) {
			this.info("Processing folder [" + fileName + "].");
			File f = new File(this.root, fileName);
			if (f.isDirectory()) {
				FileFilter ff = null;
				if (onePerFolder == OnePerFolder.XSD)
					ff = this.XSD_FILE_FILTER;
				else if (onePerFolder == OnePerFolder.XSL)
					ff = this.XSL_FILE_FILTER;
				else 
					throw new RuntimeException("Not sure how to handle type [" + onePerFolder + "]");
				
				TextFileRepo t = new TextFileRepo(f,ff);
				this.repos.add(t);
			} else {
				info("Ignoring [" + f.getAbsolutePath() + "].  See Rule 1 in the java doc for Transformations.java");
			}
		}
		
	}
	public List<TextFileRepo> getRepos() {
		return this.repos;
	}
	public String dumpToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Root folder is [" + root.getAbsolutePath()  + "]\n");
		for(TextFileRepo repo : this.repos) {
			sb.append( repo.dumpToString() );
		}
		return sb.toString();
	}
	private void info(String msg) {
		System.out.println(msg);
	}	
	public static void main(String args[]) throws IOException {
		File rootFolder = new File(args[0]);
		OnePerFolder onePerFolder = OnePerFolder.valueOf(args[1].toUpperCase());
		TextFileRepos repos = new TextFileRepos(rootFolder,onePerFolder);
		System.out.println("Loaded xsl and xml repos:\n");
		System.out.println(repos.dumpToString());
	}
	public File getFile(String fileNameCriteriaWithoutPath) {
		File foundTheFile = null;
		for(TextFileRepo r : this.repos) {
			if (r.getOnePerFolder().file.getName().equals(fileNameCriteriaWithoutPath))
				foundTheFile = r.getOnePerFolder().file;	
		}
		if (foundTheFile==null) {
			for(TextFileRepo.TextFileAndContents t : unparsedRootXsdFiles) {
				if (t.file.getName().equals(fileNameCriteriaWithoutPath)) {
					foundTheFile = t.file;
				}
			}
		}
		return foundTheFile;
	}
	/**
	 * Not designed for speed -- only expecting to be used at system startup time.
	 */
	public TextFileRepo getTextFileRepo(String repoNameCriteria) {
		TextFileRepo result  = null;
		for(TextFileRepo r : this.repos) {
			if(r.getName().equals(repoNameCriteria))
				result = r;
		}
		return result;
	}
	private String getRepoNames() {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (TextFileRepo r : this.repos) {
			if (count++>0)
				sb.append(",");
			sb.append(r.getName());
		}
		return sb.toString();
	}
	/**
	 * Not designed for speed -- only expecting to be used at system startup time.
	 */
	public StreamSource getTextFileForThisFolder(String key) {
		TextFileRepo r = this.getTextFileRepo(key);
		if (r==null) {
			throw new RuntimeException("Did not find repo key [" + key + "] in this list of repo names [" + getRepoNames() + "]");
		}
		return new StreamSource(new StringReader(r.getOnePerFolder().textFromFile));
	}
	/**
	 * Each subfolder of the root will have a 1 or more XML files and additionally have either:
	 * <ol>
	 * 	<li>One XSD per sub folder</li>
	 * 	<li>One XSL per sub folder</li>
	 * </ol>
	 * 
	 * @author erikostermueller
	 *
	 */
	public static enum OnePerFolder { XSD, XSL };
	/**
	 * 
	 * @param logger
	 * @param classPathLocation
	 * @return
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	public static File getDefaultRootFolder(String classPathLocation) throws URISyntaxException, IOException {
		File root = null;
		URL resource = TextFileRepos.class.getResource(classPathLocation);
		if (resource==null) {
			throw new RuntimeException("Could not find [" + classPathLocation + "] in the classpath using TextFileRepos.class.getResource()");
		} else {
			URI myUri = resource.toURI();
			if (myUri==null) {
				throw new RuntimeException("could not to toURI on URL [" + resource.toString() + "]");
			} 
			root = Paths.get(myUri).toFile();
		}
		return root; 
	}
	
}
class XsdFile implements FileFilter {
	public boolean accept(File pathname) {
		boolean rc = false;
		if (pathname.getAbsoluteFile().getName().endsWith(".xsd"))
			rc = true;
		return rc;
	}
}
class XslFile implements FileFilter {
	public boolean accept(File pathname) {
		boolean rc = false;
		if (pathname.getAbsoluteFile().getName().endsWith(".xsl"))
			rc = true;
		return rc;
	}
}

