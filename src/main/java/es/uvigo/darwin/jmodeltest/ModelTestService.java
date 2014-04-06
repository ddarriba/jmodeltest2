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
package es.uvigo.darwin.jmodeltest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import parser.ParseException;
import converter.Converter;
import converter.DefaultFactory;
import converter.Factory;
import es.uvigo.darwin.jmodeltest.io.NullPrintStream;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.selection.DT;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;
import es.uvigo.darwin.prottest.util.exception.AlignmentParseException;

public class ModelTestService {

	/**
     * Read alignment into a temporary file.
     * 
     * @param inputFile the file to be read
     * @param outputFile the file to write in
     * 
     * @throws AlignmentParseException the alignment parse exception.
     * @throws FileNotFoundException Signals that the input filename does not exist.
     * @throws IOException Signals that an I/O exception has occured.
     */
	public static void readAlignment(File inputFile, File outputFile)
            throws AlignmentParseException, FileNotFoundException, IOException {
		readAlignment(inputFile, outputFile, true);
	}
	
	/**
     * Read alignment.
     * 
     * @param inputFile the file to be read
     * @param outputFile the file to write in
     * 
     * @return the alignment
     * 
     * @throws AlignmentParseException the alignment parse exception.
     * @throws FileNotFoundException Signals that the input filename does not exist.
     * @throws IOException Signals that an I/O exception has occured.
     */
    public static void readAlignment(File inputFile, File outputFile, boolean deleteOnExit)
            throws AlignmentParseException, FileNotFoundException, IOException {

        StringBuilder text = new StringBuilder();

        BufferedReader br = new BufferedReader(
        		new FileReader(inputFile.getAbsolutePath()));
        String s;
        while ((s = br.readLine()) != null) {
            text.append(s).append("\r\n");
        }
        br.close();


        //Get options
        String in = text.toString();
        String inO = "";
        String inP = "";
        String inF = "";
        boolean autodetect = true;
        boolean collapse = false;
        boolean gaps = false;
        boolean missing = false;
        int limit = 0;
        String out = "";
        String outO = "Linux";
        switch (Utilities.findCurrentOS()) {
        case Utilities.OS_WINDOWS:
            outO = "Windows";
	        break;
        case Utilities.OS_OSX:
        case Utilities.OS_LINUX:
            outO = "Linux";
            break;
        }
        String outP = "PhyML";
        String outF = "PHYLIP";
        boolean lower = false;
        boolean numbers = false;
        boolean sequential = false;
        boolean match = false;

        //Get converter and convert MSA
        Factory factory = new DefaultFactory();
        Converter converter;

		PrintStream outPS = System.err;
		System.setErr(new PrintStream(new NullPrintStream()));
        try {
            converter = factory.getConverter(inO, inP, inF, autodetect,
                    collapse, gaps, missing, limit,
                    outO, outP, outF, lower, numbers, sequential, match, "ModelTestService");
            out = converter.convert(in);
        } catch (UnsupportedOperationException ex) {
            throw new AlignmentParseException("There's some error in your data: " + ex.getMessage());
        } catch (ParseException ex) {
            throw new AlignmentParseException("There's some error in your data: " + ex.getMessage());
        }
        System.setErr(outPS);
        
        if (outputFile.exists()) {
        	outputFile.delete();
        }
        outputFile.createNewFile();
        if (deleteOnExit) {
        	outputFile.deleteOnExit();
        }
        	
        FileWriter fw = new FileWriter(outputFile);
        fw.write(out);
        fw.close();
    }
    
	public InformationCriterion doIC(int ic, boolean writePAUPblock, boolean doImportances,
			boolean doModelAveraging, double confidenceInterval) {
		InformationCriterion criterion;
		switch (ic) {
		case InformationCriterion.IC_AIC:
			criterion = new AIC(writePAUPblock, doImportances,
					doModelAveraging, confidenceInterval);
			break;
		case InformationCriterion.IC_AICc:
			criterion = new AICc(writePAUPblock, doImportances,
					doModelAveraging, confidenceInterval);
			break;
		case InformationCriterion.IC_BIC:
			criterion = new BIC(writePAUPblock, doImportances,
					doModelAveraging, confidenceInterval);
			break;
		case InformationCriterion.IC_DT:
			criterion = new DT(writePAUPblock, doImportances, doModelAveraging,
					confidenceInterval);
			break;
		default:
			throw new InvalidArgumentException.UnexistentCriterionException(ic);
		}
		criterion.compute();
		
		return criterion;
	}
	
}
