package com.cloudera.hdfsutil;

import java.io.InputStream;

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
import org.apache.hadoop.security.UserGroupInformation;

public class HdfsTar {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("hdfs-tar [source-tar-gz] [dest-folder]");
        	System.exit(1);

		} else {
			System.out.println("Extracting [" + args[0] + "] to [" + args[1] + "]");
		}
		
		String source = args[0];
		String destination = args[1];

		Configuration conf = new Configuration();
	    conf.addResource(new Path("/etc/hadoop/conf/core-site.xml"));
	    conf.addResource(new Path("/etc/hadoop/conf/hdfs-site.xml"));

	    conf.set("com.sun.security.auth.module.Krb5LoginModule", "required");
	    conf.set("debug", "true");
	    
	    try {

		    UserGroupInformation.setConfiguration(conf);
		    UserGroupInformation ugi = UserGroupInformation.getLoginUser();
		    System.out.println("Current user is [" + ugi.getUserName() + "]");
		    
		    Path path = new Path(source);

	    	
	    	FileSystem fs = path.getFileSystem(conf);
		
		try (FSDataInputStream inputStream = fs.open(path)) {
		    
		    extractTarGZ(inputStream, destination, fs);
		} 

	    } catch (IllegalArgumentException e1) {
	    	System.out.println("Unable to proceed: " + e1.getMessage());
	    	System.out.println("We will now exit without extraction");
	    	System.exit(2);
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
			} else {
				System.out.println("Destination exists");
			}

			if (fs.isDirectory(base)) {
				System.out.println("Destination is a directory");

				int count = 0;
				RemoteIterator<LocatedFileStatus> files = fs.listFiles(base, true);
				while (count == 0 && files.hasNext()) {
					files.next();
					count++;
				}

				if (count > 0) {
					throw new IllegalArgumentException("Destination [" + destination + "] is not empty");
				} else {
					System.out.println("Destination [" + destination + "] is empty");
				}

			} else {
				throw new IllegalArgumentException("Destination [" + destination + "] is not a directory");
			}

			while ((entry = (TarArchiveEntry) tarIn.getNextEntry()) != null) {
				/** If the entry is a directory, create the directory. **/
				String pathText = destination + "/" + entry.getName();

				if (entry.isDirectory()) {
					System.out.println("Processing path [" + pathText + "] as directory");

					Path src = new Path(pathText);

					boolean created = fs.mkdirs(src);

					if (!created) {
						System.out.printf("Unable to create directory '%s', during extraction of archive contents.\n",
								src.toString());
					}
				} else {
					System.out.println("Processing path [" + pathText + "] as file");

					Path newFile = new Path(pathText);

					try (FSDataOutputStream output = fs.create(newFile)) {
						IOUtils.copy(tarIn, output);
					}
				}
			}

			System.out.println("Untar completed successfully!");
		}
	}
}
