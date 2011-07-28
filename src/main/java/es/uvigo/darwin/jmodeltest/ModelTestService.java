package es.uvigo.darwin.jmodeltest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import parser.ParseException;
import converter.Converter;
import converter.DefaultFactory;
import converter.Factory;
import es.uvigo.darwin.jmodeltest.selection.AIC;
import es.uvigo.darwin.jmodeltest.selection.AICc;
import es.uvigo.darwin.jmodeltest.selection.BIC;
import es.uvigo.darwin.jmodeltest.selection.DT;
import es.uvigo.darwin.jmodeltest.selection.InformationCriterion;
import es.uvigo.darwin.prottest.util.exception.AlignmentParseException;

public class ModelTestService {

	/**
     * Read alignment.
     * 
     * @param output the output writer
     * @param filename the filename
     * @param debug the debug
     * 
     * @return the alignment
     * 
     * @throws AlignmentParseException the alignment parse exception.
     * @throws FileNotFoundException Signals that the input filename does not exist.
     * @throws IOException Signals that an I/O exception has occured.
     */
    public static void readAlignment(File inputFile, File outputFile)
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
        String os = System.getProperty("os.name");
        if (os.startsWith("Mac")) {
            outO = "Linux";
        } else if (os.startsWith("Linux")) {
            outO = "Linux";
        } else if (os.startsWith("Win")) {
            outO = "Windows";
        }
        String outP = "JModelTest";
        String outF = "PHYLIP";
        boolean lower = false;
        boolean numbers = false;
        boolean sequential = false;
        boolean match = false;

        //Get converter and convert MSA
        Factory factory = new DefaultFactory();
        Converter converter;

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

        if (outputFile.exists()) {
        	outputFile.delete();
        }
        outputFile.createNewFile();
        outputFile.deleteOnExit();
        	
        FileWriter fw = new FileWriter(outputFile);
        fw.write(out);
        fw.close();
//        PushbackReader pr = new PushbackReader(new StringReader(out));
//        alignment = readAlignment(output, pr, debug);
//
//        if (alignment == null) {
//            throw new AlignmentParseException("There's some error in your data, exiting...");
//        }

//        return alignment;
    }
    
	public InformationCriterion doIC(int ic, boolean writePAUPblock, boolean doImportances,
			boolean doModelAveraging, double confidenceInterval) {
		InformationCriterion criterion;
		switch (ic) {
		case InformationCriterion.AIC:
			criterion = new AIC(writePAUPblock, doImportances,
					doModelAveraging, confidenceInterval);
			break;
		case InformationCriterion.AICc:
			criterion = new AICc(writePAUPblock, doImportances,
					doModelAveraging, confidenceInterval);
			break;
		case InformationCriterion.BIC:
			criterion = new BIC(writePAUPblock, doImportances,
					doModelAveraging, confidenceInterval);
			break;
		case InformationCriterion.DT:
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
