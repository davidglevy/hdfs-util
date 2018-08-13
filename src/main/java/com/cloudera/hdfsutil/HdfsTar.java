package com.cloudera.hdfsutil;

public class HdfsTar {

	public static void main(String[] args) {
		if (args.length < 2) {
			System.out.println("hdfs-tar [source-tar-gz] [dest-folder]");
		} else {
			System.out.println("Extracting [" + args[0] + "] to [" + args[1] + "]");
		}
	}
	
}
