//
//  InitialFocusSetter.java
//  jModelTest
//
//  Created by David Posada on 4/10/06.
//  Copyright 2006 __MyCompanyName__. All rights reserved.
//

/*

There is no straightforward way to set the initial focused component in a window. 
The typical method is to add a window listener to listen for the window opened 
event and then make the desired component request the focus.

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
