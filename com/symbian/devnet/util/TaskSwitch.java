// TaskSwitch.java
package com.symbian.devnet.util;

/**
 * Brings together four public utility methods giving Java applications 
 * access to non-AWT visual components provided by the EPOC system.
 * @version 1.2
 * @author Copyright (c) 1998-1999 Symbian Ltd.  All rights reserved.
 */
		
public class TaskSwitch   
{
	/** 
	 * Loads the taskswitch1 library from epoc32\\release\\wins\\udeb using
	 * a debug build or epoc32\\release\\wins\\urel using a release build.	 
	 */
	static {System.loadLibrary("taskswitch1");}

	/** EPOC error code */
	private static final int KErrNone = 0;      
	/** EPOC error code */
	private static final int KErrNotFound = -1;	
	/** The int for defining the error code returned */
	private static int err;
	/** Represents the current version number */ 
	private static final String VERSION_NUMBER = new String("1.2");
		
	/** Represents the native method DisplayTaskList defined in taskswitch1 */
	private static native int DisplayTaskList();
	/** Represents the native method TaskToForeground defined in taskswitch1 */
	private static native int TaskToForeground(String caption);
	/** Represents the native method InfoPrint defined in taskswitch1 */
	private static native int InfoPrint(String text);
	/** Represents the native method Notify defined in taskswitch1 */
	private static native int Notify(String line1, String line2, String but1, String but2);
	/** Represents the native method IsForeground defined in taskswitch1 */
	private static native int IsForeground(String caption);

	/** Private constructor, should not be used because all methods are static. */
	private TaskSwitch() 
	{}
	
	/** 
	 * Gets the current TaskSwitch version number
	 * @return VERSION_NUMBER, the current version number
	 */
	public static String getVersion()
	{
		return VERSION_NUMBER;
	}
	
	/** 
	 * Allows the "Open files / programs" dialog to be launched from 
	 * within Java code. As this dialog is not intended for display to end users of
	 * Quartz devices, developers should not use this method in Quartz Java programs 
	 * other than for testing or debugging purposes.
	 * <P>
	 * @exception NotFoundException if the Eikon window group is not found
	 * @throws RuntimeException if any other error condition arises in native code 
	 */
	public static void displayTaskList() throws NotFoundException
	{
		err = DisplayTaskList();
		if (err != 0) 
		{
			handleWgError(err);
		}
	}
	
	/** 
	 * Allows the Java app to find out if it is currently in the foreground. To do 
	 * this it relies on the fact that the Java app's window group (if it has one) 
	 * will always be given by the com.symbian.appName system property
	 * <P>
	 * @exception NotFoundException if the window group associated with the Java app
	 * name is not found
	 * @throws RuntimeException if any other error condition arises in native code 
	 */
	public static boolean isForeground() throws NotFoundException
	{
		int result = IsForeground(System.getProperty("com.symbian.appName"));
		if (result < 0) 
		{
			handleWgError(result); // throws an Exception
		}
		if (result == 0)
		{
			return false;
		}
		else
		{
			return true;
		}
	}

	
	/**
	 * Brings to the foreground the window group for the app with a given caption.
	 * @param caption the task to bring to the foreground
	 * @exception NotFoundException if the window group with the caption specified
	 * is not found
	 * @throws RuntimeException if any other error condition arises in native code 
	 */
	
	public static void taskToForeground(String caption) throws NotFoundException
	{
		err = TaskToForeground(caption);
		if(err != 0) 
		{
			handleWgError(err);
		}
	}
		
	/**
	 * Displays the string "text" as an info message in the top right hand corner 
	 * of the screen for a few seconds. The rest of the screen remains responsive 
	 * to user input.
	 * @param text the text which is displayed in the InfoPrint box
	 * @throws RuntimeException if any error condition arises in native code 
	 */		
	public static void infoPrint(String text)
	{
		err = InfoPrint(text);
		if(err != KErrNone) 
		{
			throw(new RuntimeException("InfoPrint failed. EPOC error code was: " + err));
		}
	}
	
	/**
	 * Displays a modal dialog asking the user for a confirmation. 
	 * @param line1 the title displayed in the dialog box
	 * @param line2 the question to be answered
	 * @param but1 the text for the negative response button
	 * @param but2 the text for the positive response button
	 * @return 0 for a "no" and 1 for a "yes"
	 * @throws RuntimeException if any error condition arises in native code 	 
	 */
	public static int notify(String line1, String line2, String but1, String but2)
	{
		int ret = Notify(line1, line2, but1, but2); 
		if (ret < 0) 
		{
			throw(new RuntimeException("Notify failed. EPOC error code was: " + ret));
		}
		else
		{
			return ret;
		}
	
	}	


	/**
	 * <p>Handles EPOC errors arising while attempting to locate non-Java window groups. 
	 * Called by {@link #displayTaskList() displayTaskList},
	 * {@link #isForeground() isForeground} and {@link #taskToForeground(java.lang.String) taskToForeground} 
	 * in response to EPOC error codes other than <code>KErrNone = 0</code>.</p>
	 * <p>A {@link com.symbian.devnet.util.NotFoundException NotFoundException}
	 * is thrown in response to EPOC error <code>KErrNotFound = -1</code>. 
	 * A <code>RuntimeException</code>, specifying the EPOC error code in its detail message 
	 * string, is thrown in all other cases.</p>
	 * @param errCode the error code returned in the calling function
	 */
	protected static void handleWgError(int errCode) throws NotFoundException
	{
		if (errCode == KErrNotFound)
		{
			throw(new NotFoundException("Window group not found"));
		}
		else 
		{
			throw(new RuntimeException("Unexpected EPOC Exception. Error code was: " + errCode));
		}
	}	
}