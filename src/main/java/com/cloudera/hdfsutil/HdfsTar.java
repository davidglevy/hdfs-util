package com.cloudera.hdfsutil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;

public class HdfsTar {

	private static final int BUFFER_SIZE = 4096;
	
	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("hdfs-tar [source-tar-gz] [dest-folder]");
		} else {
			System.out.println("Extracting [" + args[0] + "] to [" + args[1] + "]");
		}
		
		String source = args[0];
		String destination = args[1];

		Configuration conf = new Configuration();
	    conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
	    conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));

	    Path path = new Path(source);
	    
	    try {
	    FileSystem fs = path.getFileSystem(conf);
		
		try (FSDataInputStream inputStream = fs.open(path)) {
		    
		    extractTarGZ(inputStream, destination, fs);
		}

		
	    } catch (Exception e) {
	    	System.out.println("Exception processing extraction: " + e.getMessage());
	    	e.printStackTrace();
	    	System.exit(1);
	    }
	}
	
	public static void extractTarGZ(InputStream in, String destination, FileSystem fs) throws Exception {
	    GzipCompressorInputStream gzipIn = new GzipCompressorInputStream(in);
	    try (TarArchiveInputStream tarIn = new TarArchiveInputStream(gzipIn)) {
	        TarArchiveEntry entry;

	        Path base = new Path(destination);
	        if (!fs.exists(base)) {
	        	throw new IllegalArgumentException("Path doesn't exist [" + destination + "]");
	        }
	        
	        if (fs.isDirectory(base)) {
	        	int count = 0;
	        	RemoteIterator<LocatedFileStatus> files = fs.listFiles(base, false);
	        	while (files.hasNext()) {
	        		files.next();
	        		count++;
	        		break;
	        	}
	        	
	        	if (count > 0) {
	        		throw new IllegalArgumentException("Destination [" + destination + "] is not empty");
	        	}
	        	
	        } else {
	        	throw new IllegalArgumentException("Destination [" + destination + "] is not a directory");
	        }
	        
	        while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
	            /** If the entry is a directory, create the directory. **/
	            if (entry.isDirectory()) {
	                
	            	Path src = new Path(destination + "/" + entry.getName());
	            	
	            	
	            	boolean created = fs.mkdirs(src); 
	            	
	                if (!created) {
	                    System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
	                            src.toString());
	                }
	            } else {
	                Path newFile = new Path(destination + "/" + entry.getName());
	                
	                try (FSDataOutputStream output = fs.create(newFile)) {
	                	IOUtils.copy(tarIn, output);
	                }
	            }
	        }

	        System.out.println("Untar completed successfully!");
	    }
	}
}
