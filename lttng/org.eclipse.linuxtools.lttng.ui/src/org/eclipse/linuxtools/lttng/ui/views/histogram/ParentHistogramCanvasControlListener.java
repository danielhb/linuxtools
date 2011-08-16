/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   2010-06-20 Yuriy Vashchuk - Initial API and implementation
 *   
 * Modifications:
 * 2010-07-16 Yuriy Vashchuk - Base class simplification.   
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.ui.views.histogram;

import org.eclipse.swt.events.ControlEvent;

/**
 * <b><u>ParentHistogramCanvasControlListener</u></b>
 * <p>
 * Implementation of a ControlListener for the need of the ParentHistogramCanvas
 * <p> 
 */
public class ParentHistogramCanvasControlListener extends HistogramCanvasControlListener {

	private ParentHistogramCanvas ourCanvas = null;
	
	/**
	 * ParentHistogramCanvasControlListenerl constructor
	 * 
	 * @param newCanvas Related canvas
	 */
	public ParentHistogramCanvasControlListener(ParentHistogramCanvas newCanvas) {
		ourCanvas = newCanvas;
	}
	
	
	/**
	 * Method called when the canvas is resized.<p>
	 * 
	 * We need to tell the content that the canvas size changed and to recenter the windows
	 * 
	 * @param event 	The control event generated by the resize.
	 */
	@Override
	public void controlResized(ControlEvent event) {
		
		if ( (ourCanvas != null) && (ourCanvas.getHistogramContent() != null) ) {
			int newSize = ourCanvas.getSize().x;
			int oldSize = ourCanvas.getHistogramContent().getCanvasWindowSize();
			
			// Set the new canvas size
			ourCanvas.getHistogramContent().setCanvasWindowSize(newSize);
			
			// Try to recenter to window at the same place it was
			// Note : this is a best hope approach and is not intended to be precise;
			//		the idea is to avoid issuing a (maybe) long request fo the selection window;
			// There WILL be slight discrepancy between the "window values" (timestamp, etc...) showed 
			//		and what it really points to. The user should reclick by himself to refresh it. 
			int oldWindowCenter = ourCanvas.getCurrentWindow().getWindowXPositionCenter();
			int newWindowCenter = (int)Math.ceil((double)newSize * ((double)oldWindowCenter / (double)oldSize));
			ourCanvas.setWindowCenterPosition(newWindowCenter);
		}
	}
}