package com.exlibris.dps.submissionvl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.exlibris.dps.submissionvl.exceptions.HashGeneratorException;

/**
 * Helper class for generating hashes but
 * only md5 has been implemented so far
 * 
 * Contains only static methods to make them available#
 * to the rest of the application 
 * 
 * @author Lars Haendler
 *
 */

public class HashGenerator
{

	/**
	 * Constructor, unused
	 * 
	 */
	private HashGenerator()
	{
	}

	/**
	 * Static method that returns the MD5 hash for
	 * a supplied file 
	 * 
	 * @param File file for which an md5 hash is to be generated
	 * @return String md5 hash
	 * @throws HashGeneratorException unchecked user-defined exception
	 */
	public static String generateMD5(File file) throws HashGeneratorException
	{
		return hashFile(file, "MD5");
	}


	/**
	 * Static implementation of generalized has generator for any
	 * type of hashes and files
	 * 
	 * @param File file for which an md5 hash is to be generated
	 * @param String hash algorithm 
	 * @return String generated hash
	 * @throws HashGeneratorException unchecked user-defined exception
	 */
	private static String hashFile(File file, String algorithm) throws HashGeneratorException
	{
		try (FileInputStream inputStream = new FileInputStream(file))
		{
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			byte[] bytesBuffer = new byte[1024];
			int bytesRead = -1;

			while ((bytesRead = inputStream.read(bytesBuffer)) != -1)
			{
				digest.update(bytesBuffer, 0, bytesRead);
			}

			byte[] hashedBytes = digest.digest();

			return convertByteArrayToHexString(hashedBytes);

		}
		catch (NoSuchAlgorithmException | IOException e)
		{
			throw new HashGeneratorException(e);
		}

	}

	/**
	 * Static helper method for converting a byte array
	 * into a hex string
	 * 
	 * @param byte[] array of bytes
	 * @return String 
	 */
	private static String convertByteArrayToHexString(byte[] arrayBytes)
	{

		StringBuffer stringBuffer = new StringBuffer();

		for (int i = 0; i < arrayBytes.length; i++)
		{
			stringBuffer.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return stringBuffer.toString();
	}

}
