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
 * EdiarumLinkTextResolver.java - is a class to link text to a node.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.0.1
 */
package org.bbaw.telota.ediarum.extensions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import ro.sync.ecss.extensions.api.AuthorAccess;
import ro.sync.ecss.extensions.api.link.InvalidLinkException;
import ro.sync.ecss.extensions.api.link.LinkTextResolver;
import ro.sync.ecss.extensions.api.node.AttrValue;
import ro.sync.ecss.extensions.api.node.AuthorElement;
import ro.sync.ecss.extensions.api.node.AuthorNode;

public class EdiarumLinkTextResolver extends LinkTextResolver implements ContentHandler {

	/**
	 * Der AuthorAccess zu der ge�ffneten Datei
	 */
	private AuthorAccess authorAccess;
	
	/**
	 * Der Name des referenzierenden Elementes
	 */
	private String refElement = "";
	
	/**
	 * Der Name des referenzierenden Attributes 
	 */
	private String refAttr = "";
	
	/**
	 * Ein eventueller Prefix vor der referenzierenden ID
	 */
	private String refIDprefix = ":";
	
	/**
	 * Die URL zu der referenzierten Datei
	 */
	private String indexURI = "";
	
	/**
	 * Der Name des referenzierten Elementes
	 */
	private String indexElem = "";
	
	/**
	 * Der Name des referenzierten Attributes
	 */
	private String indexAttr = "";
	
	/**
	 * Ein eventueller Prefix vor der referenzierten ID
	 */
	private String indexIDprefix = "";
	
	/**
	 * Eine Variable f�r die referenzierte ID
	 */
	private String id;
	
	/**
	 * Eine Variable f�r den auszugebenden Text
	 */
	private String linkText;
	
	/**
	 * Eine Variable f�r die Verarbeitung der zu verlinkenden Datei
	 */
	private Boolean getText;

	
	/**
	 * Wird aufgerufen wenn eine entsprechende Datei ge�ffnet wird und �bergibt den AuthorAccess.
	 * Liest zudem die entsprechenden Editor Variablen aus.
	 */
	@Override
	public void activated(AuthorAccess authorAcc) {
		this.authorAccess = authorAcc;
		String linktext_url = authorAccess.getUtilAccess().expandEditorVariables("${EDIARUM_LINKTEXT_URL}", null);
		if (!linktext_url.startsWith("http")) {
//			String errorMessage = "Die ${EDIARUM_LINKTEXT_URL} ist nicht korrekt gesetzt.";
//			authorAccess.getWorkspaceAccess().showErrorMessage(errorMessage);			
		} else {
			indexURI = linktext_url;
		}
		String[] linktext_vars = (" "+authorAccess.getUtilAccess().expandEditorVariables("${EDIARUM_LINKTEXT_VARS}", null)+" ").split(",");
		if (linktext_vars.length != 6) {
//			String errorMessage = "Die ${EDIARUM_LINKTEXT_VARS} sind nicht korrekt gesetzt: " +
//					"refElement, refAttr, refIDprefix, indexElem, indexAttr, indexIDprefix";
//			authorAccess.getWorkspaceAccess().showErrorMessage(errorMessage);			
		} else {
			refElement = linktext_vars[0].trim();
			refAttr = linktext_vars[1].trim();
			refIDprefix = linktext_vars[2].trim();
			indexElem = linktext_vars[3].trim();
			indexAttr = linktext_vars[4].trim();
			indexIDprefix = linktext_vars[5].trim();
		}
	}
	
	/**
	 * Die Funktion wird �ber die CSS Funktion oxy_linktext() aufgerufen und �bergibt den momentanen Knoten.
	 * Sie verarbeitet die referenzierte Datei und gibt einen passenden Ausgabetext zur�ck.
	 * 
	 * @param node: der �bergebene Knoten
	 * @return der passende Ausgabetext
	 */
	@Override
	public String resolveReference(AuthorNode node) throws InvalidLinkException {
		// Die Variablen werden vorbereitet.
		this.clearReferencesCache();
		linkText = "";
		id = "";
		getText = false;
		// Wenn der zu verarbeitende Knoten ein Elementknoten ist, ..
		if (node.getType() == AuthorNode.NODE_TYPE_ELEMENT) {
			AuthorElement element = (AuthorElement) node;
			// .. von der richtigen Sorte ist, ..
			if (refElement.equals(element.getLocalName())) {
				// .. und das richtige Attribut besitzt, ..
				AttrValue attribute = element.getAttribute(refAttr);
				if (attribute != null) {
					if (refIDprefix.equals(attribute.getValue().substring(0, refIDprefix.length()))) {
						// .. dann lese zun�chst die ID aus dem Attribut aus.
						id = attribute.getValue().substring(refIDprefix.length());
 					try {
 						// Erzeuge eine XML-Verarbeitung ..
						XMLReader xmlReader = XMLReaderFactory.createXMLReader();
						// .. mit den richtig definierten Verarbeitungsroutinen, ..
						xmlReader.setContentHandler(this);
						// .. lese die referenzierte Datei ein, ..
						URL absoluteUrl = new URL(indexURI);
						InputSource inputSource = new InputSource(absoluteUrl.toString());
						// .. und verarbeite diese mit der definierten Routine.
 						xmlReader.parse(inputSource);
 						// K�rze schlie�lich �berfl�ssige Leerzeichen im auszugebenden Text, .. 
 						linkText = linkText.replace("\n", "").replace("\r", "").replaceAll("\\s+", " ").trim();
					} catch (SAXException e) {
						e.printStackTrace();
					} catch (MalformedURLException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
 					}
 				}
			}
		}
		// .. und gebe ihn aus.
		return linkText;
	}
	
	/**
	 * Verarbeitungsroutine f�r die XML-Starttags
	 */
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// Wenn das Element vom referenzierten Typ ist ..
		if (indexElem.equals(qName)) {
			// und die passende ID im Attribut hat, ..
			String attribute = attributes.getValue("", indexAttr);
			if ( (indexIDprefix+id).equals(attribute) ) {
				// .. dann verarbeite den kommenden Text.
				getText = true;
			}
		}
	}

	/**
	 * Verarbeitungsroutine f�r Textknoten
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		// Wenn der Text verarbeitet werden soll, ..
		if(getText) {
			// .. �bertrage ihn in den auszugebenden Text.
			String characterData = (new String(ch, start, length));
			linkText += characterData;
		}
	}

	/**
	 * Verarbeitungsroutine f�r XML-Endtags
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// Wenn das Element vom referenzierten Typ ist, ..
		if (indexElem.equals(qName)) {
			// beende die Textverarbeitung.
			getText = false;
		}
	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
	}

	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void endPrefixMapping(String arg0) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDocumentLocator(Locator locator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void skippedEntity(String name) throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub
		
	}
}
