package com.exlibris.dps.submissionvl.exceptions;

/**
 * User-defined unchecked exception for problems
 * with the HashGenerator
 * 
 * straight forward standard implementation
 * 
 * 
 * @author Lars Haendler
 *
 */
public class HashGeneratorException extends Exception
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor, just inheriting everything from Exception
	 *  
	 */
	public HashGeneratorException()
	{
		super();
	}

	/**
	 * Constructor, just inheriting everything from Exception
	 * 
	 * @param String error message
	 * @param Throwable
	 */
	public HashGeneratorException(String message, Throwable throwable)
	{
		super(message, throwable);
	}

	/**
	 * Constructor, just inheriting everything from Exception
	 * 
	 * @param String error message
	 */
	public HashGeneratorException(String message)
	{
		super(message);
	}

	/**
	 * Constructor, just inheriting everything from Exception
	 * 
	 * @param Throwable
	 */
	public HashGeneratorException(Throwable throwable)
	{
		super(throwable);
	}
}
