/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Jeff Briggs, Henry Hughes, Ryan Morse
 *******************************************************************************/

package org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.widgets;

import java.util.ArrayList;

import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.datasets.IDataSet;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.nonui.structures.GraphData;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.graphs.IGraph;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.internal.Localization;
import org.eclipse.linuxtools.systemtap.ui.graphingapi.ui.wizards.graph.GraphFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;



/**
 * A Composite type to contain a Graph object.
 * @author Henry Hughes
 * @author Ryan Morse
 */
public class GraphComposite extends Composite {
	/**
	 * The default constructor: creates an internal composite for the Graph to render on, asks GraphFactory
	 * to create the graph from the given GraphData and DataSet, then initializes all buttons and listeners.
	 */
	public GraphComposite(Composite parent, int style, GraphData gd, IDataSet ds) {
		super(parent, style);
		FormLayout layout = new FormLayout();
		layout.marginWidth = 5;
		layout.marginHeight = 5;
		this.setLayout(layout);
		checkOptions = new ArrayList<Button>();

		label = new Label(this, SWT.HORIZONTAL | SWT.CENTER);
		scale = scales[7];
		label.setText(scale + Localization.getString("GraphComposite.ScaleValue"));
		FormData data = new FormData();
		data.top = new FormAttachment(0,0);
		data.right = new FormAttachment(100, 0);
		data.width = 40;
		label.setLayoutData(data);

		zoomScale = new Scale(this,SWT.VERTICAL | SWT.BORDER);
		zoomScale.setMinimum(0);
		zoomScale.setMaximum(14);
		zoomScale.setIncrement(1);
		zoomScale.setPageIncrement(1);
		zoomScale.setSelection(7);
		data = new FormData();
		data.top = new FormAttachment(label, 10);
		data.bottom = new FormAttachment(100, 5);
		data.right = new FormAttachment(100,-10);
		zoomScale.setLayoutData(data);

		graph = GraphFactory.createGraph(this, gd, ds);
		data = new FormData();
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		data.right = new FormAttachment(label,-10);
		data.left = new FormAttachment(0,0);
		graph.setLayoutData(data);

		graph.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		graph.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
		this.setBackground(getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		zoomScale.addSelectionListener(scaleListener);
	}

	/**
	 * Toggles sidebar visible or not visible.
	 * @param withSidebar Enables or disables the sidebar.
	 */
	public void configure(boolean withSidebar) {
		sidebarVisible = withSidebar;
		
		for(int i=0; i<checkOptions.size(); i++)
			((Button)checkOptions.get(i)).setVisible(sidebarVisible);
		
		zoomScale.setVisible(sidebarVisible);
		label.setVisible(sidebarVisible);

		FormData data = new FormData();
		data.top = new FormAttachment(0,0);
		data.bottom = new FormAttachment(100,0);
		data.left = new FormAttachment(0,0);
		data.right = (withSidebar ? new FormAttachment(label,-10) : new FormAttachment(100, 0));
		graph.setLayoutData(data);

		layout(true, true);
	}
	
	public void addCheckOption(String title, SelectionListener listener) {
		Button b = new Button(this, SWT.CHECK);
		b.setText(title);

		Button old = null;
		if(checkOptions.size() > 0)
			old = (Button)checkOptions.get(checkOptions.size()-1);

		FormData data = new FormData();
		data.bottom = (null != old) ? new FormAttachment(old,0) : new FormAttachment(100, 0);
		data.right = new FormAttachment(100,0);
		data.width = 85;
		b.setLayoutData(data);
		b.addSelectionListener(listener);
		
		checkOptions.add(b);

		if(checkOptions.size() == 1) {
			((FormData)label.getLayoutData()).right = new FormAttachment(b, 0);
			((FormData)zoomScale.getLayoutData()).right = new FormAttachment(b, 0);
		}
	}
	
	public void removeCheckOption(String title) {
		Button b;
		for(int i=0; i<checkOptions.size(); i++) {
			b = (Button)checkOptions.get(i);
			if(b.getText().equals(title)) {
				checkOptions.remove(b);
				
				if(checkOptions.size() == 0)
					((FormData)label.getLayoutData()).right = new FormAttachment(100, 0);

				return;
			}
		}
	}
	
	/**
	 * Returns the graph that is rendering to this composite.
	 */
	public IGraph getGraph() {
		return graph;
	}

	/**
	 * Returns the current sidebar visibility state.
	 */
	public boolean isSidebarVisible() {
		return sidebarVisible;
	}
	
	/**
	 * Dispose is overriden in order to dispose of the listeners attached to this Composite on disposal.
	 */
	public void dispose() {
		scaleListener = null;

		if(null != graph) graph.dispose();
		graph = null;
		
		if(null != zoomScale) zoomScale.dispose();
		zoomScale = null;
		
		if(null != label) label.dispose();
		label = null;
		super.dispose();
	}

	/*
	 * Listeners are below:
	 * 	scaleListener - detects movement in the Scale widget and rescales the graph on change
	 *  titleListener - A SelectionListener for the title button
	 *  legendListener - A SelectionListener for the legend checkbox
	 *  gridListener - A SelectionListener for the Grid checkbox
	 *  normalizeListener - A SelectionListener for the normalization checkbox
	 *  propertyChangeListener - Detects changes in user preferences and applies them
	 */
	private SelectionListener scaleListener = new SelectionListener() {
		public void widgetSelected(SelectionEvent e) {
			Scale scaler = (Scale) e.widget;
			int index = scaler.getSelection();
			if(scale != scales[index]) {
				scale = scales[index];
				label.setText(scale + Localization.getString("GraphComposite.ScaleValue"));
				graph.setScale(scale);
			}
		}

		public void widgetDefaultSelected(SelectionEvent e) {}
	};
	
	private boolean sidebarVisible = true;
	private IGraph graph;
	private Scale zoomScale;
	public double scale;
	private static final double[] scales = { .0625, .125, .25, .33 , .5, .66, .8, 1.0, 1.25, 1.5, 2, 3, 4, 8, 16 }; 
	private Label label;
	
	private ArrayList<Button> checkOptions;
}