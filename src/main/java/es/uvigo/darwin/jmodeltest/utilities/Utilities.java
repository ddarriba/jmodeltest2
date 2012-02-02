/*
Copyright (C) 2011  Diego Darriba, David Posada

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package es.uvigo.darwin.jmodeltest.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.text.Document;

import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.gui.XManager;

public final class Utilities {

	public static final int NA = Integer.MIN_VALUE;

	public Utilities() {
	}

	public static String lastToken(String str) {
		StringTokenizer st = new StringTokenizer(str);
		String token = "";
		while (st.hasMoreTokens()) {
			token = st.nextToken();
		}
		return token;
	}

	public static String secondToLastToken(String str) {
		StringTokenizer st = new StringTokenizer(str);
		int numTokens = st.countTokens();
		int tokenNumber = 0;
		String token = "";

		while (st.hasMoreTokens()) {
			token = st.nextToken();
			tokenNumber++;
			if (tokenNumber == numTokens - 1) {
				break;
			}
		}
		return token;
	}

	public static boolean isWindows() {
		if (System.getProperty("os.name").startsWith("Window"))
			return true;
		return false;
	}

	public static String findCurrentOS() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Mac"))
			return "macintosh";
		else if (os.startsWith("Windows"))
			return "windows";
		else
			return "linux";
	}

	public static String getBinaryVersion() {
		String os = System.getProperty("os.name");
		String arch = System.getProperty("os.arch");
		String bit = System.getProperty("sun.arch.data.model");

		if (os.startsWith("Mac") && arch.startsWith("ppc")) {
			System.err.println("Sorry, PowerPC architecture is no longer supported");
			System.exit(0);
		} else if (os.startsWith("Mac")) {
			return "PhyML_3.0_macOS_i386";
		} else if (os.startsWith("Linux")) {
			if (bit.startsWith("64"))
				return "PhyML_3.0_linux64";
			else
				return "PhyML_3.0_linux32";
		} else if (os.startsWith("Windows")) {
			return "PhyML_3.0_win32.exe";
		}
		return null;
	}

	public static String calculateRuntimeMinutes(long startTime, long endTime) {
		long seconds = (long) Math.round((endTime - startTime) / 1000.0);
		int hours = (int) (seconds / 3600.0);
		int rest1 = (int) (seconds % (3600.0));
		int minutes = (int) (rest1 / 60.0);
		seconds = (int) (seconds - (hours * 3600 + minutes * 60));
		String h = "" + hours;
		String m = "" + minutes;
		String s = "" + seconds;
		if (minutes < 10)
			m = "0" + m;
		if (seconds < 10)
			s = "0" + s;
		return h + "h:" + m + ":" + s + "";
	}

	public static String displayRuntime(long time) {
		long decimes = (long) Math.round(time / 100.0);
		int hours = (int) (decimes / 36000.0);
		int rest1 = (int) (decimes % 36000.0);
		int minutes = (int) (rest1 / 600.0);
		int rest2 = (int) (rest1 % 600.0);
		int seconds = (int) (rest2 / 10.0);
		decimes = (int) (decimes - (hours * 36000 + minutes * 600 + seconds * 10));
		String h = "" + hours;
		String m = "" + minutes;
		String s = "" + seconds;
		String d = "0" + decimes;
		if (hours < 10)
			h = "0" + h;
		if (minutes < 10)
			m = "0" + m;
		if (seconds < 10)
			s = "0" + s;
		return h + "h:" + m + ":" + s + ":" + d + "";
	}
	
	public static String calculateRuntime(long startTime, long endTime) {
		return displayRuntime(endTime - startTime);
	}

	public static String getPath() {
		return (new Utilities()).internalGetPath(false);
	}

	public static String getURLPath() {
		return (new Utilities()).internalGetPath(true);
	}

	private String internalGetPath(boolean withFile) {
		// ClassLoader loader = this.getClass().getClassLoader();
		// URL tmp = loader.getResource("./");
		String j = null;
		URL tmp = null;
		try {
			// tmp = XProtTest.class.getResource("");
			tmp = getClass().getResource("");
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
			return null;
		}
		if (tmp == null) {
			System.err
					.println("***************************************************************");
			System.err
					.println("** ERROR: Cannot find ModelTest's path, unable to run phyml!! **");
			System.err
					.println("***************************************************************");
			// prottest.setValue(-1);
			return null;
		} else {
			j = tmp.getPath();
			j = replace(j, "%20", " ");
			// j = tmp.getPath().replaceAll("%20", " ");
			// System.err.println("j: " + j);
			if (!withFile) {
				if (isWindows())
					j = replace(j, "file:/", "");
				else
					j = replace(j, "file:", "");
			}
			// j = Pattern.compile("file:").matcher(j).replaceFirst("");
			// j = j.replaceFirst("file:", "");
			// System.err.println("after replaceFirst:  " + j);
			// j = Pattern.compile("!.*$").matcher(j).replaceFirst("");
			int last2 = j.lastIndexOf("!");
			j = j.substring(0, last2);
			// j = j.replaceFirst("!.*$", "");
			// System.err.println("after replaceFirst2: " + j);
			int last = j.lastIndexOf("/");
			// j = j.replaceFirst("/.*?.jar$", "");
			j = j.substring(0, last);
			// System.err.println("after replaceFirst3: " + j);
		}
		return j;
	}

	public static String quoteIt(String orig) {
		// quote spaces (or other characters) of filenames:
		orig = replace(orig, " ", "\\ ");
		return orig;
	}

	/**
	 * I took this method from SkeetUtil (
	 * http://www.yoda.arachsys.com/java/skeetutil/ )
	 */
	public static String replace(String orig, String from, String to) {
		int fromLength = from.length();

		if (fromLength == 0)
			throw new IllegalArgumentException(
					"String to be replaced must not be empty");

		int start = orig.indexOf(from);
		if (start == -1)
			return orig;

		boolean greaterLength = (to.length() >= fromLength);

		StringBuffer buffer;
		// If the "to" parameter is longer than (or
		// as long as) "from", the final length will
		// be at least as large
		if (greaterLength) {
			if (from.equals(to))
				return orig;
			buffer = new StringBuffer(orig.length());
		} else {
			buffer = new StringBuffer();
		}

		char[] origChars = orig.toCharArray();

		int copyFrom = 0;
		while (start != -1) {
			buffer.append(origChars, copyFrom, start - copyFrom);
			buffer.append(to);
			copyFrom = start + fromLength;
			start = orig.indexOf(from, copyFrom);
		}
		buffer.append(origChars, copyFrom, origChars.length - copyFrom);

		return buffer.toString();
	}

	public static boolean isNumber(String s) {
		String validChars = "0123456789.";
		boolean isNumber = true;

		for (int i = 0; i < s.length() && isNumber; i++) {
			char c = s.charAt(i);
			if (validChars.indexOf(c) == -1)
				isNumber = false;
			else
				isNumber = true;
		}
		return isNumber;
	}

	public static void toConsoleEnd() {
		if (ModelTest.buildGUI)
			XManager.getInstance()
					.getPane()
					.setCaretPosition(
							XManager.getInstance().getPane().getDocument()
									.getLength());
	}

	/**
	 * copyFile
	 * 
	 * Copies src file to dst file. If the dst file does not exist, it is
	 * created modified from The Java Developers Almanac 1.4:
	 * http://www.exampledepot.com/egs/java.io/CopyFile.html
	 */

	public static void copyFile(String source, String destination)
			throws IOException {
		File src = new File(source);
		File dst = new File(destination);

		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);

		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	/**
	 * CheckNA
	 * 
	 * If value is NA it prints "-"
	 */
	public static String CheckNA(double value) {
		if (value == NA)
			return "  -  ";
		else {
			String s = String.format(Locale.ENGLISH, "%8.4f", value);
			return s;
		}
	}

	/**
	 * deleteFile
	 * 
	 * Delete a given file
	 */
	public static void deleteFile(String fileName) {

		File f;

		try {
			f = new File(fileName);
		} catch (Exception e) {
			throw new IllegalArgumentException("DeleteFiles: deletion of "
					+ fileName + " failed");
		}

		// Make sure the file or directory exists and isn't write protected
		if (f.exists()) {
			if (!f.canWrite())
				throw new IllegalArgumentException(
						"DeleteFiles: write protected: " + fileName);

			// if it is a directory, make sure it is empty
			if (f.isDirectory()) {
				String[] files = f.list();
				if (files.length > 0)
					throw new IllegalArgumentException(
							"DeleteFiles: directory not empty: " + fileName);
			}

			// explicit close associated stream for windows
			//
			// attempt to delete it
			boolean success = f.delete();

			if (!success)
				throw new IllegalArgumentException("DeleteFiles: deletion of "
						+ fileName + " failed");
		}
	}

	/**
	 * printRed
	 * 
	 * Prints to the main console text in red
	 */

	public static void printRed(String text) {
		if (ModelTest.buildGUI) {
			try {
				Document doc = XManager.getInstance().getPane().getDocument();
				doc.insertString(doc.getLength(), text, XManager.redText);
			} catch (javax.swing.text.BadLocationException e) {
			}
		}
	}

	/******************
	 * RoundDoubleTo **************************
	 * 
	 * Rounds a double to a number of significant digits
	 *********************************************************/

	public static double RoundDoubleTo(double decimal, int dplaces) {
		BigDecimal bd = new BigDecimal(decimal);
		bd = bd.setScale(dplaces, BigDecimal.ROUND_UP);
		return bd.doubleValue();
	}

	/******************
	 * putSlashBeforeSpaces **************************
	 * 
	 * Inserts slash before paces in a path (MacOS X)
	 *********************************************************/

	public static String putSlashBeforeSpaces(String originalPath) {
		if (originalPath == null)
			return "";

		StringBuffer s = new StringBuffer((String) originalPath);

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ' ')
				s.insert(i++, '\\');
		}
		return s.toString();
	}

	/******************
	 * substituteSpaces **************************
	 * 
	 * substitutes %20 in spaces in a path (MacOS X)
	 *********************************************************/

	public static String substituteSpaces(String originalPath) {
		if (originalPath == null)
			return "";

		StringBuffer s = new StringBuffer((String) originalPath);

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == ' ') {
				s.setCharAt(i, '0');
				s.insert(i++, '%');
				s.insert(i++, '2');
			}
		}
		return s.toString();
	}

	/******************
	 * putQuotesAroundSpaces **************************
	 * 
	 * Inserts quotes at correct locations in nominated path string. (Windows)
	 *********************************************************/

	public static String putQuotesAroundSpaces(String originalPath) {
		StringTokenizer st = new StringTokenizer(originalPath, File.separator);
		String newPath = "\"";
		while (st.hasMoreTokens()) {
			String token = st.nextToken();
			if (token.contains(" ")) {
				newPath += File.separator + "\"" + token + "\"";
			} else {
				newPath += File.separator + token;
			}
		}
		return newPath.substring(1, newPath.length());
	}

	/******************
	 * specialConcatStringArrays **************************
	 * 
	 * concatenates two string arrays but removinf the first string from the
	 * second
	 *********************************************************/

	public static String[] specialConcatStringArrays(String[] A, String[] B) {
		String[] C = new String[A.length + B.length - 1];
		System.arraycopy(A, 0, C, 0, A.length);
		System.arraycopy(B, 1, C, A.length, B.length - 1);
		return C;
	}

} // end of class