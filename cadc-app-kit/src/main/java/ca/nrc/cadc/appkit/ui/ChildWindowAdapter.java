/*
 * Copyright (c) 2000, 2005 by Patrick Dowler.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the 
 *
 * Free Software Foundation, Inc.
 * 59 Temple Place - Suite 330
 * Boston, MA  02111-1307, USA.
 *
 * http://www.gnu.org
 */

package ca.nrc.cadc.appkit.ui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Calls dispose() on the source Window when it receives a
 * window-closing event. This is handy for child windows
 * of the application that should just be nuked immediately.
 *
 * @author Patrick Dowler
 * @version 1.0
 */
public class ChildWindowAdapter extends WindowAdapter {
    private boolean gcOnClose = false;

    public ChildWindowAdapter() {
    }

    public ChildWindowAdapter(boolean gcOnClose) {
        this.gcOnClose = gcOnClose;
    }

    public void windowClosing(WindowEvent e) {
        e.getWindow().dispose();
    }
}

