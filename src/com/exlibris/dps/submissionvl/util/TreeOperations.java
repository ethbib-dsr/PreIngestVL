package com.exlibris.dps.submissionvl.util;

import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.attribute.*;
import static java.nio.file.FileVisitResult.*;
import java.io.IOException;
import java.util.*;

/**
 * Sample code that copies files in a similar manner to the cp(1) program.
 */

public class TreeOperations
{
	private TreeOperations()
	{
		
	}
	
	/**
	 * Copy source file to target location. If {@code prompt} is true then prompt
	 * user to overwrite target if it exists. The {@code preserve} parameter
	 * determines if file attributes should be copied/preserved.
	 */
	static void copyFile(Path source, Path target)
	{

		CopyOption[] options = new CopyOption[]{ REPLACE_EXISTING };

		try
		{
			Files.copy(source, target, options);
		}
		catch (IOException x)
		{
			System.err.format("Unable to copy: %s: %s%n", source, x);
		}

	}
	
	
	public static void deleteTree(String deletePath) throws IOException
	{
		Path directory = Paths.get(deletePath);

		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
		   @Override
		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			   Files.delete(file);
			   return FileVisitResult.CONTINUE;
		   }

		   @Override
		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			   if(exc==null)
			   {
			   	Files.delete(dir);
				   return FileVisitResult.CONTINUE;			   	
			   }
			   else
			   {
			   	return FileVisitResult.TERMINATE;
			   }
		   }
	   });		
	}
	
	
	public static void copyTree(String sourcePath, String targetPath) throws IOException
	{
		boolean preserve = true;

		// remaining arguments are the source files(s) and the target location
		Path source = Paths.get(sourcePath);
		Path target = Paths.get(targetPath); 

		// copy each source file/directory to target
		EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
		TreeCopier tc = new TreeCopier(source, target, preserve);
		Files.walkFileTree(source, opts, Integer.MAX_VALUE, tc);
	}	

	/**
	 * A {@code FileVisitor} that copies a file-tree ("cp -r")
	 */
	static class TreeCopier implements FileVisitor<Path>
	{
		private final Path source;
		private final Path target;
		private final boolean preserve;


		TreeCopier(Path source, Path target, boolean preserve)
		{
			this.source = source;
			this.target = target;
			this.preserve = preserve;
		}


		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
		{
			// before visiting entries in a directory we copy the directory
			// (okay if directory already exists).
			CopyOption[] options = (preserve) ? new CopyOption[]
			{ COPY_ATTRIBUTES } : new CopyOption[0];

			Path newdir = target.resolve(source.relativize(dir));
			try
			{
				Files.copy(dir, newdir, options);
			}
			catch (FileAlreadyExistsException x)
			{
				// ignore
			}
			catch (IOException x)
			{
				System.err.format("Unable to create: %s: %s%n", newdir, x);
				return SKIP_SUBTREE;
			}
			return CONTINUE;
		}


		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
		{
			copyFile(file, target.resolve(source.relativize(file)));
			return CONTINUE;
		}


		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
		{
			// fix up modification time of directory when done
			if (exc == null && preserve)
			{
				Path newdir = target.resolve(source.relativize(dir));
				try
				{
					FileTime time = Files.getLastModifiedTime(dir);
					Files.setLastModifiedTime(newdir, time);
				}
				catch (IOException x)
				{
					System.err.format("Unable to copy all attributes to: %s: %s%n", newdir, x);
				}
			}
			return CONTINUE;
		}


		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
		{
			if (exc instanceof FileSystemLoopException)
			{
				System.err.println("cycle detected: " + file);
			}
			else
			{
				System.err.format("Unable to copy: %s: %s%n", file, exc);
			}
			return CONTINUE;
		}
	}

}