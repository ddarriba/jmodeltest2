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
