package org.eclipse.linuxtools.rpm.ui.editor;

import org.eclipse.jface.text.rules.IWordDetector;

public class MacroWordDetector implements IWordDetector {

	public boolean isWordPart(char c) {
		// TODO Auto-generated method stub
		return (Character.isLetterOrDigit(c) || c == '%' || c == '{' || c == '}' || c == '_');
	}

	public boolean isWordStart(char c) {
		// TODO Auto-generated method stub
		return (c == '%');
	}

}