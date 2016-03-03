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
package es.uvigo.darwin.jmodeltest.io;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.PushbackReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import pal.alignment.Alignment;
import pal.alignment.ReadAlignment;
import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.exception.AlignmentParseException;

/**
 * AlignmentReader.java
 * 
 * Description: Class for parsing MSA files
 * 
 * @author Diego Darriba, University of Vigo / University of A Coruna, Spain
 *         ddarriba@udc.es
 * @author David Posada, University of Vigo, Spain dposada@uvigo.es |
 *         darwin.uvigo.es
 * @version 2.1.10 (Mar 2016)
 */
public abstract class AlignmentReader {
	private static ApplicationOptions options = ApplicationOptions
			.getInstance();;

	/****************************
	 * readDataFile **************************** * Reads the input file and gets
	 * the number of taxa and alignment * length * *
	 ***********************************************************************/

	static public void getHeader(String infilenameComplete) {
		// needs the complete path to the file
		try {
			TextInputStream in = new TextInputStream(infilenameComplete);
			String line = in.readLine();
			in.close();
			StringTokenizer reader = new StringTokenizer(line);
			options.setNumTaxa(Integer.parseInt(reader.nextToken()));
			options.setNumSites(Integer.parseInt(reader.nextToken()));
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(new JFrame(),
					"Could not read the input alignment", "jModelTest error",
					JOptionPane.ERROR_MESSAGE);
		}

		if (options.getNumTaxa() <= 4)
			JOptionPane.showMessageDialog(new JFrame(),
					"The number of taxa does not seem to be correct: "
							+ options.getNumTaxa(), "jModelTest error",
					JOptionPane.ERROR_MESSAGE);

		if (options.getNumSites() <= 1)
			JOptionPane.showMessageDialog(new JFrame(),
					"The number of sites does not seem to be correct: "
							+ options.getNumTaxa(), "jModelTest error",
					JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Creates an alignment instance.
	 * 
	 * @param out
	 *            the out
	 * @param pr
	 *            the pushback reader which contains the alignment
	 * @param debug
	 *            the debug state
	 * 
	 * @return the alignment
	 * 
	 * @throws AlignmentParseException
	 *             the alignment parse exception
	 * @throws IOException
	 *             Signals that an I/O exception has occured.
	 */
	public static Alignment createAlignment(PrintWriter out, PushbackReader pr,
			boolean debug) throws AlignmentParseException, IOException {

		if (debug) {
			out.println("");
			out.println("**********************************************************");
			out.println("  Reading alignment...");
		}
		Alignment alignment = null;
		try {
			alignment = new ReadAlignment(pr);
		} catch (pal.alignment.AlignmentParseException e) {
			throw new AlignmentParseException(e.getMessage());
		}

		List<String> seqNames = new ArrayList<String>(
				alignment.getSequenceCount());
		for (int i = 0; i < alignment.getSequenceCount(); i++) {
			seqNames.add(alignment.getIdentifier(i).getName());
		}
		
		if (debug) {
			for (int i = 0; i < alignment.getSequenceCount(); i++) {
				out.println("    Sequence #" + (i + 1) + ": "
						+ alignment.getIdentifier(i).getName());
			}
			out.println("   Alignment contains " + alignment.getSequenceCount()
					+ " sequences of length " + alignment.getSiteCount());
			out.println("");
			out.println("**********************************************************");
			out.println("");
		}

		return alignment;
	}
} // class AlignmentReader

