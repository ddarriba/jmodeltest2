package es.uvigo.darwin.jmodeltest.io;

import iubio.readseq.BioseqFormats;
import iubio.readseq.BioseqWriterIface;
import iubio.readseq.Readseq;

import java.io.File;
import java.io.FileOutputStream;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;

public abstract class AlignmentReader {
	private static ApplicationOptions options = ApplicationOptions.getInstance();;

	/****************************
	 * readDataFile **************************** * Reads the input file and gets
	 * the number of taxa and alignment * length * *
	 ***********************************************************************/

	static public void getHeader(String infilenameComplete) {
		// needs the complete path to the file
		TextInputStream in = new TextInputStream(infilenameComplete);
		String line = in.readLine();
		in.close();
		StringTokenizer reader = new StringTokenizer(line);
		options.numTaxa = Integer.parseInt(reader.nextToken());
		options.numSites = Integer.parseInt(reader.nextToken());

		if (options.numTaxa <= 4)
			JOptionPane.showMessageDialog(new JFrame(),
					"The number of taxa does not seem to be correct: "
							+ options.numTaxa, "jModelTest error",
					JOptionPane.ERROR_MESSAGE);

		if (options.numSites <= 1)
			JOptionPane.showMessageDialog(new JFrame(),
					"The number of sites does not seem to be correct: "
							+ options.numTaxa, "jModelTest error",
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
				TextInputStream in = new TextInputStream(outputFile.getAbsolutePath());
				String line = in.readLine();
				in.close();
				StringTokenizer reader = new StringTokenizer(line);
				options.numTaxa = Integer.parseInt(reader.nextToken());
				options.numSites = Integer.parseInt(reader.nextToken());
				options.numBranches = 2 * options.numTaxa - 3;
				goodFile = true;

				if (options.numTaxa < 4) {
					JOptionPane.showMessageDialog(new JFrame(),
							"The number of taxa (" + options.numTaxa
									+ ") does not seem to be correct!",
							"jModeltest error", JOptionPane.ERROR_MESSAGE);
					goodFile = false;
				}

				if (options.numSites <= 1) {
					JOptionPane.showMessageDialog(new JFrame(),
							"The number of sites (" + options.numSites
									+ ") does not seem to be correct!",
							"jModeltest error", JOptionPane.ERROR_MESSAGE);
					goodFile = false;
				}

			} else {
				JOptionPane.showMessageDialog(new JFrame(),
						"Cannot read or import the file: " + inputFile.getName(),
						"jModeltest error", JOptionPane.ERROR_MESSAGE);
			}

		}

		catch (Exception e) {
			e.printStackTrace();
		}

		return goodFile;
	}

} // class AlignmentReader

