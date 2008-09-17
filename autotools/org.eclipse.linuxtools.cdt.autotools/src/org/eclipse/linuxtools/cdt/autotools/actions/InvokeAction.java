/*******************************************************************************
 * Copyright (c) 2006, 2007 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.cdt.autotools.actions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public abstract class InvokeAction extends AbstractTargetAction {

	protected void showInformation(String title, String content) {

		MessageDialog.openInformation(new Shell(), title, content);
	}

	protected void showError(String title, String content) {

		MessageDialog.openError(new Shell(), title, content);
	}

	protected void showSuccess(String title) {
		MessageDialog.openConfirm(new Shell(), title, 
				InvokeMessages.getString("InvokeAction.success")); //$NON-NLS-1$
	}
	
	protected String showInput(String title, String content, String defaultTxt) {
		InputDialog getOptionDialog = new InputDialog(new Shell(), title,
				content, defaultTxt, null);

		getOptionDialog.open();

		return getOptionDialog.getValue();
	}

	/**
	 * Separate targets to array from a string.
	 * 
	 * @param rawArgList
	 * @return targets in string[] array. if targets are not formatted properly,
	 *         returns null
	 */
	protected String[] separateTargets(String rawArgList) {

		StringTokenizer st = new StringTokenizer(rawArgList, " "); //$NON-NLS-1$
		ArrayList targetList = new ArrayList();

		while (st.hasMoreTokens()) {
			String currentWord = st.nextToken().trim();

			if (currentWord.startsWith("'")) { //$NON-NLS-1$
				String tmpTarget = ""; //$NON-NLS-1$
				while (!currentWord.endsWith("'")) { //$NON-NLS-1$
					tmpTarget += currentWord + " "; //$NON-NLS-1$
					if (!st.hasMoreTokens()) {
						// quote not closed properly, so return null
						return null;
					}
					currentWord = st.nextToken().trim();
				}

				tmpTarget += currentWord;
				targetList.add(tmpTarget);
				continue;
			}

			if (currentWord.startsWith("\"")) { //$NON-NLS-1$
				String tmpTarget = ""; //$NON-NLS-1$
				while (!currentWord.endsWith("\"")) { //$NON-NLS-1$
					tmpTarget += currentWord + " "; //$NON-NLS-1$
					if (!st.hasMoreTokens()) {
						// double quote not closed properly, so return null
						return null;
					}
					currentWord = st.nextToken().trim();
				}

				tmpTarget += currentWord;
				targetList.add(tmpTarget);
				continue;
			}

			// for targets without quote/double quotes.
			targetList.add(currentWord);

		}

		return (String[])targetList.toArray(new String[targetList.size()]);
	}

	protected String[] separateOptions(String rawArgList) {
		ArrayList argList = new ArrayList();
		// May be multiple user-specified options in which case we
		// need to split them up into individual options
		rawArgList = rawArgList.trim();
		boolean finished = false;
		int lastIndex = rawArgList.indexOf("--"); //$NON-NLS-1$
		if (lastIndex != -1) {
			while (!finished) {
				int index = rawArgList.indexOf("--", lastIndex + 2); //$NON-NLS-1$
				if (index != -1) {
					String previous = rawArgList.substring(lastIndex, index)
							.trim();
					argList.add(previous);
					rawArgList = rawArgList.substring(index);
				} else {
					argList.add(rawArgList);
					finished = true;
				}
			}
		}

		return (String[])argList.toArray(new String[argList.size()]);

	}

	protected IPath getExecDir(IContainer container) {
		int type = container.getType();
		IPath execDir = null;
		if (type == IContainer.FILE) {
			execDir = container.getLocation().removeLastSegments(1);
		} else {
			execDir = container.getLocation();
		}
		return execDir;
	}
	
	protected HashMap executeCommand(IPath command,
			String[] argumentList, String[] envList, IPath execDir) {

		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		CommandLauncher cmdL = new CommandLauncher();

		// invoke command
		Process automakeProcess = cmdL.execute(command, argumentList, envList,
				execDir);

		if (cmdL.waitAndRead(stdout, stderr) == CommandLauncher.OK) {
			try {
				automakeProcess.getOutputStream().close();
			} catch (IOException e) {
				// ignore
			}
		} else {
			// failed to execute command
			showError(InvokeMessages
									.getString("InvokeAction.execute.windowTitle.error"), InvokeMessages //$NON-NLS-1$
									.getString("InvokeAction.execute.message") //$NON-NLS-1$
									+ command.toOSString()); //$NON-NLS-1$
			return null;
		}

		HashMap outputs = new HashMap();

		outputs.put("stdout", stdout.toString()); //$NON-NLS-1$
		outputs.put("stderr", stderr.toString()); //$NON-NLS-1$

		try {
			stdout.close();
			stderr.close();
		} catch (IOException e) {
			// ignore
		}

		return outputs;
	}
}