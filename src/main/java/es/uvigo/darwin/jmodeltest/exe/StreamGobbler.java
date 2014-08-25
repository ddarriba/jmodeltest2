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
package es.uvigo.darwin.jmodeltest.exe;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import es.uvigo.darwin.jmodeltest.ApplicationOptions;
import es.uvigo.darwin.jmodeltest.utilities.Utilities;

class StreamGobbler extends Thread {
	private InputStream is;
	private String type;
	private OutputStream osFile, osConsole;
	private static int runId = 1;
	private int localRunId;
	
	public int getRunId() {
		return localRunId;
	}
	
	StreamGobbler(InputStream is, String type, OutputStream fileRedirect,
			OutputStream consoleRedirect) {
		this.is = is;
		this.type = type;
		this.osFile = fileRedirect;
		this.osConsole = consoleRedirect;
		synchronized(is) {
			this.localRunId = runId++;
		}
	}

	public void run() {
		int i=0;
		try {
			PrintWriter pwFile = null;
			PrintWriter pwConsole = null;
			if (osFile != null)
				pwFile = new PrintWriter(osFile);
			if (osConsole != null)
				pwConsole = new PrintWriter(osConsole);

			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line = null;
			while ((line = br.readLine()) != null) {
				if (line.contains("patterns found")) {
					try {
						int numPatterns = Integer.parseInt(Utilities
								.firstNumericToken(line));
						ApplicationOptions options = ApplicationOptions
								.getInstance();
						if (Math.abs(options.getNumPatterns()) == 0) {
							options.setNumPatterns(numPatterns);
						} else {
							if (Math.abs(options.getNumPatterns() - numPatterns) > 0) {
								// number of patterns has changed!!!
								// temporary number of patterns is updated
								options.setNumPatterns(numPatterns);
							}
						}
					} catch (NumberFormatException nfe) {
						// ignore
					}

				}
				if (pwFile != null) {
					synchronized (pwFile) {
						pwFile.println(type + "("+localRunId+")>" + line);
						pwFile.flush();
					}
				}
				if (pwConsole != null) {
						pwConsole.println(type + "("+localRunId+")>" + line);
						pwConsole.flush();
				}
			}
			if (pwConsole != null)
				pwConsole.flush();
		} catch (IOException ioe) {
			System.err.println("INFO: [StreamGobbler] The stream was closed!");
		}
	}
}
