package soba.core;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	public static String getMD5(byte[] bytearray) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			byte[] hash = digest.digest(bytearray);
			return getString(hash);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	private static String getString(byte[] bytearray) {
		StringBuilder b = new StringBuilder();
		for (int i=0; i<bytearray.length; ++i) {
			String s = "0" + Integer.toHexString(bytearray[i]);
			b.append(s.substring(s.length()-2));
		}
		return b.toString();
	}
}
