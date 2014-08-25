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

import iubio.readseq.BioseqFormats;
import iubio.readseq.BioseqWriterIface;
import iubio.readseq.Readseq;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;

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
					"Could not read the input alignment",
					"jModelTest error",
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

	/****************************
	 * readAlignment **************************** * Reads a DNA alignment using
	 * ReadSeq * * *
	 ***********************************************************************/

	// DP check: phylip input with long names

	static public boolean readDataFile(File inputFile, File outputFile) {
		boolean goodFile = false;
		try {
			int outid = BioseqFormats.formatFromName("phylip");
			BioseqWriterIface seqwriter = BioseqFormats.newWriter(outid);
			// seqwriter.setOutput(System.out);

			FileOutputStream dataphy = new FileOutputStream(outputFile);
			seqwriter.setOutput(dataphy);
			seqwriter.writeHeader();
			Readseq rd = new Readseq();
			// rd.verbose = true;
			rd.setInputObject(inputFile.getAbsolutePath());

			if (rd.isKnownFormat() && rd.readInit() && rd.getFormat() != 13) // not
																				// plain
			{
				rd.readTo(seqwriter);
				seqwriter.writeTrailer();

				/*
				 * SeqFileInfo info = rd.getInfo(); seqlen = info.seqlen; nseq =
				 * info.nseq; //this does not work nseq = rd.nresults;//this
				 * does not work
				 * 
				 * System.err.println ("Format name = " + rd.getFormatName());
				 * System.err.println ("NumSeq = " + nseq ); System.err.println
				 * ("Length = " + seqlen);
				 */

				// get alignment info and check it is valid for jModeltest
				TextInputStream in = new TextInputStream(
						outputFile.getAbsolutePath());
				String line = in.readLine();
				in.close();
				StringTokenizer reader = new StringTokenizer(line);
				options.setNumTaxa(Integer.parseInt(reader.nextToken()));
				options.setNumSites(Integer.parseInt(reader.nextToken()));
				options.setNumBranches(2 * options.getNumTaxa() - 3);
				goodFile = true;

				if (options.getNumTaxa() < 4) {
					JOptionPane.showMessageDialog(new JFrame(),
							"The number of taxa (" + options.getNumTaxa()
									+ ") does not seem to be correct!",
							"jModeltest error", JOptionPane.ERROR_MESSAGE);
					goodFile = false;
				}

				if (options.getNumSites() <= 1) {
					JOptionPane.showMessageDialog(new JFrame(),
							"The number of sites (" + options.getNumSites()
									+ ") does not seem to be correct!",
							"jModeltest error", JOptionPane.ERROR_MESSAGE);
					goodFile = false;
				}

			} else {
				JOptionPane.showMessageDialog(
						new JFrame(),
						"Cannot read or import the file: "
								+ inputFile.getName(), "jModeltest error",
						JOptionPane.ERROR_MESSAGE);
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return goodFile;
	}
} // class AlignmentReader

