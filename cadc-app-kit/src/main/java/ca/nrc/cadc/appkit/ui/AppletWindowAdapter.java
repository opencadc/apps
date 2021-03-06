// Created on 14-Jul-2005

package ca.nrc.cadc.appkit.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Simple WindowAdaptor implementation for use with AppletFrame.
 * This class does some simple things in response to events. For
 * windowClosing, it calls AppletFrame.quit().
 *
 * @author Patrick Dowler
 * @version 1.0
 */
public class AppletWindowAdapter extends WindowAdapter {
    private AppletFrame af;

    public AppletWindowAdapter(AppletFrame af) {
        super();
        this.af = af;
    }

    public void windowClosing(WindowEvent e) {
        af.quit();
    }
}
