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
package es.uvigo.darwin.jmodeltest.utilities;

import java.awt.Component;
import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

 public class InitialFocusSetter {
        public static void setInitialFocus(Window w, Component c) {
            w.addWindowListener(new FocusSetter(c));
        }
    
        public static class FocusSetter extends WindowAdapter {
            Component initComp;
            FocusSetter(Component c) {
                initComp = c;
            }
            public void windowOpened(WindowEvent e) {
                initComp.requestFocus();
    
                // Since this listener is no longer needed, remove it
                e.getWindow().removeWindowListener(this);
            }
        }
    }
