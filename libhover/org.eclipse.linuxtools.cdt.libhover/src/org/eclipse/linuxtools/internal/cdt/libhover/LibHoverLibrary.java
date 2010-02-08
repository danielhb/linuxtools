/*******************************************************************************
 * Copyright (c) 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.internal.cdt.libhover;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.cdt.libhover.ClassInfo;
import org.eclipse.linuxtools.cdt.libhover.FunctionInfo;
import org.eclipse.linuxtools.cdt.libhover.LibHoverInfo;
import org.eclipse.linuxtools.cdt.libhover.LibhoverPlugin;
import org.eclipse.linuxtools.cdt.libhover.TypedefInfo;

public class LibHoverLibrary {
	private String name;
	private String location;
	private String docs;
	private boolean isCPP;
	private LibHoverInfo hoverInfo = new LibHoverInfo();
	private boolean haveReadHoverInfo = false;

	public LibHoverLibrary(String name, String location, String docs, 
			boolean isCPP) {
		this.name = name;
		this.location = location;
		this.docs = docs;
		this.isCPP = isCPP;
	}
	
	/**
	 * Get the name of the library
	 * 
	 * @return the name of the library
	 */
	public String getName() { 
		return name; 
	}
	
	/**
	 * Get the location of the library hover info (URL or file name)
	 * 
	 * @return the URL or file name string for the library hover info location
	 */
	public String getLocation() {
		return location;
	}
	
	/**
	 * Get the browser help documentation URL for this library
	 * 
	 * @return the name of the help documentation URL
	 */
	public String getDocs() {
		return docs;
	}
	
	/**
	 * Is this library a C++ library?
	 * 
	 * @return true if C++ library, false otherwise
	 */
	public boolean isCPP() {
		return isCPP;
	}

	/**
	 * Get the library hover info for this library
	 * 
	 * @return the library hover info for this library
	 */
	public LibHoverInfo getHoverInfo() {
		// We lazily get the hover info for this library since it is possible
		// the user will never access or ultimately need it if another library
		// supplies the information first.
		if (!haveReadHoverInfo) {
			URI acDoc;
			try {
				acDoc = new URI(location);
				IPath p = URIUtil.toPath(acDoc);
				InputStream docStream = null;
				if (p == null) {
					URL url = acDoc.toURL();
					docStream = url.openStream();
				} else {
					try {
						// Try to open the file as local to this plug-in.
						docStream = FileLocator.openStream(LibhoverPlugin.getDefault().getBundle(), p, false);
					} catch (IOException e) {
						// File is not local to plug-in, try file system.
						docStream = new FileInputStream(p.toFile());
					}
				}
				ObjectInputStream input = new ObjectInputStream(docStream);
				hoverInfo = (LibHoverInfo)input.readObject();
				haveReadHoverInfo = true;
				input.close();
				docStream.close();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hoverInfo;
	}
	
	/**
	 * Set the library hover info.
	 * 
	 * @param hoverInfo the library hover info to set
	 */
	public void setHoverinfo(LibHoverInfo hoverInfo) {
		this.hoverInfo = hoverInfo;
	}
	
	/**
	 * Fetch the class info for a given class.
	 * 
	 * @param className the name of the class to fetch info for
	 * @return ClassInfo or null if no class info can be found
	 */
	public ClassInfo getClassInfo(String className, ArrayList<String> templateTypes) {
		String typedefName = className.replaceAll("<.*>", "<>"); // $NON-NLS-1$ // $NON-NLS-2$
		TypedefInfo typedef = getHoverInfo().typedefs.get(typedefName);
		if (typedef != null) {
			className = typedef.getTransformedType(className);  // Reset class name to typedef transformation
		}
		int index = className.indexOf('<');
		// Check if it is a template reference.
		if (index != -1) {
			resolveTemplateTypes(className, templateTypes, index);
			// It is.  We want to see if there are partial specific templates
			// and we choose the first match.  If nothing matches our particular
			// case, we fall back on the initial generic template.
			ClassInfo info = getHoverInfo().classes.get(className.substring(0, index));
			ArrayList<ClassInfo> children = info.getChildren();
			if (children != null && children.size() > 0) {
				for (int x = 0; x < children.size(); ++x) {
					ClassInfo child = children.get(x);
					if (className.matches(child.getClassName())) {
						info = child;
						break;
					}
				}
			}
			return info;
		}
		// Otherwise no template, just fetch the class info directly.
		return getHoverInfo().classes.get(className);
	}
	
	private void resolveTemplateTypes(String className,
			ArrayList<String> templateTypes, int index) {
		int startIndex = index + 1;
		int i = startIndex;
		int count = 1;
		while (count > 0 && i < className.length()) {
			char x = className.charAt(i);
			switch (x) {
			case ('<'):
				++count;
			break;
			case ('>'):
				--count;
				if (count == 0)
					templateTypes.add(className.substring(startIndex, i).trim());
			break;
			case (','): {
				if (count == 1) {
					templateTypes.add(className.substring(startIndex, i).trim());
					startIndex = i + 1;
				}
			}
			break;
			}
			++i;
		}
	}

	/**
	 * Fetch the function info for a given function.
	 * 
	 * @param name of function to find info for
	 * @return FunctionInfo or null if no function info exists
	 */
	public FunctionInfo getFunctionInfo(String name) {
		return getHoverInfo().functions.get(name);
	}
}
