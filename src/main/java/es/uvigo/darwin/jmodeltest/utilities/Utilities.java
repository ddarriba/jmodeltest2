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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.StringTokenizer;

import javax.swing.text.Document;

import pal.alignment.Alignment;
import es.uvigo.darwin.jmodeltest.ModelTest;
import es.uvigo.darwin.jmodeltest.gui.XManager;
import es.uvigo.darwin.prottest.util.exception.AlignmentParseException;
import es.uvigo.darwin.prottest.util.fileio.AlignmentReader;

public final class Utilities {

	public static final int NA = Integer.MIN_VALUE;
	public static final int OS_LINUX = 1;
	public static final int OS_OSX = 2;
	public static final int OS_WINDOWS = 3;

	public Utilities() {
	}

	public static String firstNumericToken(String str) {
		StringTokenizer st = new StringTokenizer(str);
		String token = "";
		boolean found = false;
		while (st.hasMoreTokens() && !found) {
			token = st.nextToken();
			found |= isNumber(token);
		}
		if (found)
			return token;
		else
			return null;
	}
	
	public static String lastToken(String str) {
		StringTokenizer st = new StringTokenizer(str);
		String token = "";
		while (st.hasMoreTokens()) {
			token = st.nextToken();
		}
		return token;
	}

	public static boolean isWindows() {
		if (System.getProperty("os.name").startsWith("Window"))
			return true;
		return false;
	}

	public static int findCurrentOS() {
		String os = System.getProperty("os.name");
		if (os.startsWith("Mac"))
			return OS_OSX;
		else if (os.startsWith("Windows"))
			return OS_WINDOWS;
		else
			return OS_LINUX;
	}

	public static String getBinaryVersion() {
		String arch = System.getProperty("os.arch");
		String bit = System.getProperty("sun.arch.data.model");

		String binaryName = null;
		switch (findCurrentOS()) {

		case OS_OSX:
			if (arch.startsWith("ppc")) {
				System.err
						.println("Sorry, PowerPC architecture is no longer supported");
				System.exit(0);
			} else {
				binaryName = "PhyML_3.0_macOS_i386";
			}
			break;
		case OS_LINUX:
			if (bit.startsWith("64"))
				binaryName = "PhyML_3.0_linux64";
			else
				binaryName = "PhyML_3.0_linux32";
			break;
		case OS_WINDOWS:
			binaryName = "PhyML_3.0_win32.exe";
			break;
		default:
			binaryName = null;
		}

		return binaryName;
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

	public static String getCurrentTime(String format) {
		Calendar cal = new GregorianCalendar();
	    SimpleDateFormat date_format = new SimpleDateFormat(format);
	    return date_format.format(cal.getTime());
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

	public static boolean isNumber(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException ex) {
			return false;
		}
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
	public static String checkNA(double value) {
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

	public static double roundDoubleTo(double decimal, int dplaces) {
		BigDecimal bd = new BigDecimal(decimal);
		bd = bd.setScale(dplaces, BigDecimal.ROUND_UP);
		return bd.doubleValue();
	}

	public static String asPercent(double decimal) {
		if (decimal > 100.0d) {
			decimal = 100.0d;
		}
		return format(decimal, 6, 2, false) + "%";
	}

	public static String format(double number, int totalLength, int decimalPlaces, boolean exp) {
		StringBuffer sb;
		String format;
		if (exp) {
			format = "%"+totalLength+"."+decimalPlaces+"e";
		} else {
			format = "%"+totalLength+"."+decimalPlaces+"f";
		}
		sb = new StringBuffer(String.format(Locale.ENGLISH, format, number));
		// normalize string to size 6
		for (int i = sb.length(); i < totalLength; i++) {
			sb.insert(0, " ");
		}

		return sb.toString();
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

	/**
	 * Calculate invariable sites.
	 * 
	 * @param alignment the alignment
	 * 
	 * @return the invariable sites in the alignment
	 */
	public static int calculateInvariableSites (Alignment alignment) {

		//use this function to estimate a good starting value for the InvariableSites distribution.
		int numSites    = alignment.getSiteCount();
		int numSeqs     = alignment.getSequenceCount();
		int inv         = 0;
		int tmp;
		boolean tmpInv = true;
		for(int i=0; i < numSites; i++) {
			tmp     = indexOfChar(alignment.getData(0,i));
			tmpInv  = true;
			for(int j=0; j < numSeqs; j++) {
				if(indexOfChar(alignment.getData(j,i)) != tmp) { //if at least one difference in column i:
					j = numSeqs;    //we exit this for.
					tmpInv = false; //i is not an invariable site.
				}
			}
			if(tmpInv)
				inv++;
		}
		return inv;
	}
	
	private static int indexOfChar (char c) {
		char[] charSet = {'A', 'C', 'G', 'T', 'a', 'c', 'g', 't'};
		for (int charIndex = 0; charIndex < charSet.length; charIndex++)
			if (charSet[charIndex] == c)
				return charIndex % 4;

		return -1;
	}
	
	public static double calculateShannonSampleSize (Alignment alignment, boolean doJustShannonEntropy) {

		int numSites = alignment.getSiteCount();
		int numTaxa = alignment.getSequenceCount();
		
		//int    pattern[][]    = new int   [numSites][numSeqs];
		double freqs  [][]    = new double[numSites][4];
		byte   state  [][]    = new byte  [numSites][4];
		double siteS  []      = new double[numSites];
		int    sequences[]    = new int[numSites];
		double shannonEntropy = 0.0;

		//We simply count bases at positions and store in state[][]
		for(int i=0; i < numSites; i++) {
			for(int j=0; j < numTaxa; j++) {
				//state[i][indexOfchar(alignment.getData(j,i))]++;
				int index = indexOfChar(alignment.getData(j,i));
				if(index >= 0) {
					state[i][index]++;
					sequences[i]++;
				}
				//state[i][sP.pattern[j][i]]++;
			}
		}

		//For each alignment position, we calculate aminoacid frequencies. And also...
		//For each alignment position, we calculate Shannon Entropy based on previous frequencies.
		for(int i=0; i < numSites; i++) {
			for(int j=0; j < 4; j++) {
				//freqs[i][j] = (double)state[i][j]/(double)numSeqs;
				freqs[i][j] = (double)state[i][j]/(double)sequences[i];
				if(freqs[i][j] > 0)
					siteS[i]   += freqs[i][j]*Math.log(freqs[i][j])/Math.log(2);
			}
		}

		//We sum positions entropies over the whole alignment.
		for(int i=0; i < numSites; i++) {
			shannonEntropy += siteS[i];
		}

		if(doJustShannonEntropy) {
			return -1.0*shannonEntropy; //sum of shannon Entropy positions.
		} else { //if Options.SHANNON_NxL
			double meanShannonEntropy;
			double maxShannonEntropy = 0;
			double normalizedShannonEntropy = 0;
			meanShannonEntropy = -1.0*shannonEntropy/(double)numSites; //mean S for sites
			//let's normalize ShannonEntropy from 0 to 1:
			for(int i=0; i<4; i++) {
				maxShannonEntropy += (1.0/(double)4)*Math.log(1.0/(double)4)/Math.log(2);
			}
			//by this moment we normalize by a "rule of three"
			normalizedShannonEntropy = -1.0*meanShannonEntropy/maxShannonEntropy;
			return (double)numSites*(double)numTaxa*normalizedShannonEntropy; //NxL x averaged Shannon entropy
		}
	}
	
	public static double calculateShannonSampleSize (File alignmentFile, boolean doJustShannonEntropy) {
		Alignment alignment = null;
		try {
			alignment = AlignmentReader.readAlignment(
					new PrintWriter(System.err),
					alignmentFile.getAbsolutePath(), false);
		} catch (AlignmentParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return (calculateShannonSampleSize(alignment, doJustShannonEntropy));
	}
} // end of class