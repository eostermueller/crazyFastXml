package com.github.eostermueller.util;

import java.io.File;
import java.io.FileFilter;
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
	public File directory = null;
	FileFilter allFiles = null;
	FileFilter onePerFolderFilter = null;
	FileFilter xmlFileFilter = new FileFilter() {
		public boolean accept(File pathname) {
			boolean rc = false;
			if (pathname.getAbsolutePath().toLowerCase().endsWith(".xml"))
					rc = true;
			return rc;
		}
	};
	TextFileRepo(File directory, FileFilter val) throws IOException {
		
		this.onePerFolderFilter = val;
		allFiles = new FileFilter() {

			public boolean accept(File pathname) {
				boolean rc = false;
				if (onePerFolderFilter.accept(pathname) || xmlFileFilter.accept(pathname))
					rc = true;
				return rc;
			}
		};
		this.directory = directory;
		if (!directory.isDirectory()) {
			throw new RuntimeException("The given file [" + directory.toString() + "] must be a directory.");
		}
		File[] files = directory.listFiles(this.allFiles);
		for(File f : files) {
			if (f.isFile()) {
				if (this.onePerFolderFilter.accept(f)) {
					if (this.getOnePerFolder()!=null) {
						throw new RuntimeException("Expected only one file to To match " + this.onePerFolderFilter.getClass().getName() + "].  Instead, found at least two:  [" + f.getAbsolutePath() + "] and [" + this.getOnePerFolder().file.getAbsolutePath() + "]");
					}
					this.setOnePerFolder(new TextFileAndContents(f));
				} else if (this.xmlFileFilter.accept(f)) {
					this.getXmlFiles().add( new TextFileAndContents(f));
				} else {
					info("Ignoring file [" + f.getAbsolutePath() + ".  Only using .xml [" + this.onePerFolderFilter.getClass().getName() + "]");
				}
			}
		}
		if (this.getOnePerFolder()==null) {
			throw new RuntimeException("Exactly one file ending in [" + this.onePerFolderFilter.getClass().getName() + "] must be in the folder [" + directory.getAbsolutePath() + "].  Didn't find any.");
		}
		if (this.getXmlFiles().size() ==0) {
			throw new RuntimeException("Expected to find at least one file that ends in .xml in the directory [" + directory.getAbsolutePath() + "]");
		}
	}
	
	private void info(String msg) {
		System.out.println(msg);	
	}
	private TextFileAndContents oneFilePerFolder;
	List<TextFileAndContents> xmlFiles = new ArrayList<TextFileAndContents>();
	public TextFileAndContents getOnePerFolder() {
		return oneFilePerFolder;
	}
	public void setOnePerFolder(TextFileAndContents file) {
		this.oneFilePerFolder = file;
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
		sb.append("\t\tOneFilePerFolder file [" + this.getOnePerFolder().file.getName() + "] with [" + this.getXmlFiles().size() + "] xml files\n");
		for(TextFileAndContents f : this.getXmlFiles()) {
			sb.append("\t\t\tXML File [" + f.file.getName() + "]\n");
		}
			
 		return sb.toString();
	}
	public static class TextFileAndContents {
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
