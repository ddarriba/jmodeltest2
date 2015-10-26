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

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class ProcessManager {

	private static ProcessManager instance;
	private List<Process> processes;

	private ProcessManager() {
		processes = new ArrayList<Process>();
	}

	public synchronized void registerProcess(Process process) {
		synchronized (processes) {
			processes.add(process);
		}
	}

	public synchronized void removeProcess(Process process) {
		synchronized (processes) {
			processes.remove(process);
		}
	}

	public synchronized void killAll() {
		try {
			synchronized (processes) {
				for (Process p : processes) {
					p.destroy();
					processes.remove(p);
				}
			}
		} catch (ConcurrentModificationException ex) {
			// Ignore... this sometimes happens
		}
	}

	public synchronized static ProcessManager getInstance() {
		if (instance == null) {
			instance = new ProcessManager();
		}
		return instance;
	}
}
