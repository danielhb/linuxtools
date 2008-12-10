/*******************************************************************************
 * Copyright (c) 2006 Phil Muldoon <pkmuldoon@picobot.org>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Phil Muldoon <pmuldoon@redhat.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.changelog.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * @author pmuldoon (Phil Muldoon)
 */
public interface IParserChangeLogContrib {

	/**
	 * Used to determine function name from a currently open editor, where the
	 * cursor is at. Used by KeyAction.
	 * 
	 * @param editor
	 * @return
	 * @throws CoreException
	 */
	public String parseCurrentFunction(IEditorPart editor) throws CoreException;

	/**
	 * Used to determine function name from and editor input, with offset
	 * supplied manualy. Used by prepare changelog.
	 * 
	 * @param input
	 * @param offset
	 * @return
	 * @throws CoreException
	 */
	public String parseCurrentFunction(IEditorInput input, int offset)
			throws CoreException;

}