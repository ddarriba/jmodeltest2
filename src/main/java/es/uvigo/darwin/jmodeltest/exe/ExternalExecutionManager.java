/*
Copyright (C) 2009  Diego Darriba

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
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
import java.util.Collection;

/**
 * A thrid-party applications manager to control proccesses running on
 * the machine and kill them when necessary.
 * 
 * @author Diego Darriba
 * @since 3.0
 */
public class ExternalExecutionManager {

    /** Unique instance of the manager */
    private static ExternalExecutionManager instance;
    /** Collection of running processes */
    private final Collection<Process> processes;

    /**
     * Instantiates a new execution manager
     */
    private ExternalExecutionManager() {
        this.processes = new ArrayList<Process>();
    }

    /**
     * Gets the unique instance of the class
     * 
     * @return the ExternalExecutionManager instance
     */
    public static ExternalExecutionManager getInstance() {
        if (instance == null) {
            instance = new ExternalExecutionManager();
        }
        return instance;
    }

    /**
     * Adds a process in execution to the collection
     * 
     * @param proc the running process
     * 
     * @return true, if successfully added the process
     */
    public boolean addProcess(Process proc) {
        boolean result = false;
        if (!processes.contains(proc)) {
            result = processes.add(proc);
        }
        return result;
    }

    /**
     * Removes a process from the collection
     * 
     * @param proc the process to remove
     * 
     * @return true, if successfully removed the process
     */
    public boolean removeProcess(Process proc) {
        boolean result = false;
        if (processes.contains(proc)) {
            result = processes.remove(proc);
        }
        return result;
    }

    /**
     * Kills all running processes in the collection
     */
    public void killProcesses() {
        for (final Process proc : processes) {
            if (proc != null) {
                try {
                    proc.exitValue();
                } catch (IllegalThreadStateException e) {
                    // The process is executing, so we should kill it
                    Runtime.getRuntime().addShutdownHook(
                            new Thread(new Runnable() {

                        public void run() {
                            proc.destroy();
                        }
                    }));
                }
            }
        }
        processes.clear();
    }
}
