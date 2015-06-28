package com.github.eostermueller.xslt;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores file system path info for a single xsl file and 1 or more xml files
 * that the xsl can transform using an xslt engine.
 * This class assumes that both the xsl and xml files will all be located in a single folder 
 * on the file system..
 * Assumption:  the single folder contains one file that ends in .xsl.
 * 
 * @author erikostermueller
 *
 */
public class TextFileRepo {
	File directory = null;
	TextFileRepo(File directory) throws IOException {
		this.directory = directory;
		if (!directory.isDirectory()) {
			throw new RuntimeException("The given file [" + directory.toString() + "] must be a directory.");
		}
		String[] files = directory.list();
		for(String file : files) {
			File f = new File(directory,file);
			if (f.isFile()) {
				if (f.getAbsolutePath().endsWith("xsl")) {
					if (this.getXsl()!=null) {
						throw new RuntimeException("Expected only one .xsl file to be in folder [" + directory.getAbsolutePath() + "].  Instead, found at least two:  [" + f.getAbsolutePath() + "] and [" + this.getXsl().file.getAbsolutePath() + "]");
					}
					this.setXsl(new TextFileAndContents(f));
				} else if (f.getAbsolutePath().endsWith("xml")) {
					this.getXmlFiles().add( new TextFileAndContents(f));
				} else {
					info("Ignoring file [" + f.getAbsolutePath() + ".  Only using .xml and .xsl files");
				}
			}
		}
		if (this.getXsl()==null) {
			throw new RuntimeException("Exactly one file ending in .xsl must be in the folder [" + directory.getAbsolutePath() + "].  Didn't find any.");
		}
		if (this.getXmlFiles().size() ==0) {
			throw new RuntimeException("Expected to find at least one file that ends in .xml in the directory [" + directory.getAbsolutePath() + "]");
		}
	}
	
	private void info(String msg) {
		System.out.println(msg);	
	}
	private TextFileAndContents xsl;
	List<TextFileAndContents> xmlFiles = new ArrayList<TextFileAndContents>();
	public TextFileAndContents getXsl() {
		return xsl;
	}
	public void setXsl(TextFileAndContents xsl) {
		this.xsl = xsl;
	}
	public List<TextFileAndContents> getXmlFiles() {
		return xmlFiles;
	}
	public void setXmlFiles(List<TextFileAndContents> xmlFiles) {
		this.xmlFiles = xmlFiles;
	}

	public String getName() {
		return this.directory.getName();
	}
	public Object dumpToString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Folder for repo [" + this.getName() + "]\n");
		sb.append("\t\tXSL file [" + this.getXsl().file.getName() + "] with [" + this.getXmlFiles().size() + "] xml files\n");
		for(TextFileAndContents f : this.getXmlFiles()) {
			sb.append("\t\t\tXML File [" + f.file.getName() + "]\n");
		}
			
 		return sb.toString();
	}
	static class TextFileAndContents {
		public TextFileAndContents(File f) throws IOException {
			this.textFromFile = this.getFileContents(f);
			file = f;
		}
		public File file;
		public String textFromFile;
		public String getFileContents(File f) throws IOException {
			return new String(Files.readAllBytes(Paths.get(f.getAbsolutePath())), StandardCharsets.UTF_8);
		}
	}}
