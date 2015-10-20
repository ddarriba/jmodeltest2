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

} // class AlignmentReader

