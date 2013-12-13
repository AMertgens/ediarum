/**************************************************************************
 *  Copyright notice
 *	
 *  ediarum - an Oxygen XML Author framework for digital scholarly editions
 *  Copyright (C) 2013 Berlin-Brandenburg Academy of Sciences and Humanities
 *	
 *  This file is part of ediarum; ediarum is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ediarum is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with ediarum.  If not, see <http://www.gnu.org/licenses/>.
***************************************************************************/

/**
 * SurroundWithElement.java - is a class for inserting elements before and after a selection.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.2
 */
package org.bbaw.telota.ediarum;

import ro.sync.ecss.extensions.api.ArgumentDescriptor;
import ro.sync.ecss.extensions.api.ArgumentsMap;
import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.AuthorOperation;
import ro.sync.ecss.extensions.api.AuthorOperationException;

public class SurroundWithElementsOperation implements AuthorOperation {
	/**
	 * Argument describing the elements.
	 */
	private static final String ARGUMENT_ELEMENTS = "elements";
	
	/**
	 * Argument describing the elements.
	 */
	private static final String ARGUMENT_ID = "id";
	
	/**
	 * Arguments.
	 */
	private static final ArgumentDescriptor[] ARGUMENTS = new ArgumentDescriptor[] {
		new ArgumentDescriptor(
				ARGUMENT_ID,
				ArgumentDescriptor.TYPE_STRING,
				"Eine in den Elementen mehrfach zu verwendende ID"),
		new ArgumentDescriptor(
				ARGUMENT_ELEMENTS,
				ArgumentDescriptor.TYPE_STRING,
				"Die vor und hinter der Markierung einzuf�genden Elemente durch '$[SELECTION]' getrennt, die ID wird mit '$[ID]' eingef�gt ")
	};
	  
	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#doOperation(AuthorAccess, ArgumentsMap)
	 */
	public void doOperation(AuthorAccess authorAccess, ArgumentsMap args) throws AuthorOperationException {
		Object elementsArgVal = args.getArgumentValue(ARGUMENT_ELEMENTS);
		Object idArgVal = args.getArgumentValue(ARGUMENT_ID);
		// Die �bergebenen Parameter werden �berpr�ft.
		if (idArgVal == null) {
			idArgVal = "";
		}
		if (elementsArgVal != null
				&& elementsArgVal instanceof String
				&& idArgVal != null
				&& idArgVal instanceof String) {
			// Falls im Text nichts selektiert ist, wird das aktuelle Word ausgew�hlt. 
			if (!authorAccess.getEditorAccess().hasSelection()) {
	    		authorAccess.getEditorAccess().selectWord();
	    	}
	    	int selStart = authorAccess.getEditorAccess().getSelectionStart();
	    	int selEnd = authorAccess.getEditorAccess().getSelectionEnd();
			
	    	// Die ID wird an den entsprechenden Stellen eingef�gt.
	    	String[] elementsInParts = ((String)elementsArgVal).split("\\$\\[ID\\]");
	    	String elementsWithID = elementsInParts[0];
	    	for (int i=1; i<elementsInParts.length; i++) {
	    		elementsWithID += idArgVal + elementsInParts[i];
	    	}	    	
	    	// Die beiden Elemente im �bergebenen Argument werden getrennt, ..
	    	String[] elements = elementsWithID.split("\\$\\[SELECTION\\]"); 
			
	    	// .. das erste wird vor der Selektion eingef�gt, und das zweite dahinter.
	    	authorAccess.getDocumentController().insertXMLFragment((String) elements[1], selEnd);
	    	authorAccess.getDocumentController().insertXMLFragment((String) elements[0], selStart);
			
	    } else {
	    	throw new IllegalArgumentException(
	    			"One or more of the argument values are not declared, they are: elements - " + elementsArgVal
	    			+ ", id - " + idArgVal);
	    }
	}
	
	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getArguments()
	 */
	public ArgumentDescriptor[] getArguments() {
		return ARGUMENTS;
	}
	  	
	/**
	 * @see ro.sync.ecss.extensions.api.AuthorOperation#getDescription()
	 */
	public String getDescription() {
		return "F�gt vor und hinter der Markierung verschiedene Elemente ein.";
	}
}
