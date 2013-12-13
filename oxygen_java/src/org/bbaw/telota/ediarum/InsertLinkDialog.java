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
 * InsertLinkDialog.java - is a class for opening a dialog to select a file and a reference.
 * It belongs to package ro.sync.ecss.extensions.ediarum for the modification of the Oxygen framework
 * for several projects at the Berlin-Brandenburgische Akademie der Wissenschaften (BBAW) to build a
 * framework for edition projects (ediarum). 
 * @author Martin Fechner
 * @version 1.1.1
 */
package org.bbaw.telota.ediarum;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.List;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JButton;
import javax.swing.JDialog;

public class InsertLinkDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6533369934664023577L;

	/**
	 * Dies sind die Parameter f�r die Fenstergr��e des Dialogs.
	 */
	static int H_SIZE = 400;
	static int V_SIZE = 300;

	/**
	 * Dies sind die privaten Variablen.
	 * fileListe Dies ist das Auswahlfeld mit den Dateien.
	 * linkListe Dies ist das Auswahlfeld mit den Verweiszielen.
	 * files Enth�lt die Dateinamen.
	 * filesIDs Enth�lt die Dateiidentfikatoren.
	 * links Enth�lt die Verweiseziele.
	 * IDs Enth�lt die IDs der Verweisziele.
	 * selectedID Enth�lt die ID des ausgew�hlten Verweiszieles.  
	 * selectedFile Enth�lt den ausgew�hlten Dateinamen.
	 * selectedFileID Enth�lt die ID der ausgew�hlten Datei.
	 */
	List fileListe = new List();
	List linkListe = new List();
	String[] files;
	String[] filesIDs;
	String[][] links;
	String[][] IDs;
	String selectedID = "";
	String selectedFile = "";
	String selectedFileID = "";

	/**
	 * Der Konstruktor erzeugt ein Dialogfenster zur Auswahl eines Verweiszieles aus verschiedenen Dateien.
	 * @param parent Das �bergeordnete Fenster
	 * @param file Ein Array, das alle Dateinamen enth�lt
	 * @param fileID Ein Array, das die IDs zu den Dateien enth�lt
	 * @param refs Ein mehrdimensionales Array, das die Texte der Verweisziele enth�lt
	 * @param ids Ein mehrdimensionales Array, das die IDs der Verweisziele enth�lt
	 */
	public InsertLinkDialog(Frame parent, String[] file, String[] fileID, String[][] refs, String[][] ids) {
		// Ein modales Fenster wird erzeugt.
		super(parent, true);
		// F�r den Dialog wird das Layout (North, South, .., Center) ausgew�hlt und der Titel gesetzt.
		setLayout(new BorderLayout());
		setTitle("Querverweis einf�gen");

		// Die �bergebenen Parameter werden in die privaten Variablen eingelesen.
		files = file;
		filesIDs = fileID;
		links = refs;
		IDs = ids;

		// Oben wird ein Auswahlfeld mit den offenen Dateien erzeugt, ..
		fileListe.setMultipleMode(false);
		for (int i=0; i<file.length; i++){
			fileListe.add(file[i]);
		}
		fileListe.addItemListener(new fileListListener());
		add("North", fileListe);

		// .. in der Mitte wird ein zun�chst leeres Auswahlfeld f�r die Verweisen einer Datei erzeugt, ..
		linkListe.setMultipleMode(false);
		add("Center", linkListe);

		// und unten werden die Kn�pfe "Ok" und "Abbrechen" eingesetzt.
		Panel panel = new Panel();
		JButton ok = new JButton("Ok");
		ok.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent arg0) {
			okAction();
		}});
		panel.add(ok);
		JButton cancel = new JButton("Abbrechen");
		cancel.addActionListener(new ActionListener(){public void actionPerformed(ActionEvent arg0) {
			cancelAction();
		}});
		panel.add(cancel);
		add("South", panel);
		getRootPane().setDefaultButton(ok);

		// Die Eigenschaften des Dialogfenster werden angepa�t: die Gr��e, der Ort und die Sichtbarkeit.
		setSize(H_SIZE, V_SIZE);
		setLocation( (parent.getBounds().width - H_SIZE) /2, (parent.getBounds().height - V_SIZE) /2);
		setVisible(true);

	}

	/**
	 * Bei "Ok" merkt sich der Dialog die ID und den Namen der ausgew�hlten
	 * Datei, sowie evtl. die ID eines ausgew�hlten Verweises und schlie�t das Fenster.
	 */
	public void okAction(){
		selectedFile = files[fileListe.getSelectedIndex()];
		selectedFileID = filesIDs[fileListe.getSelectedIndex()];
		if (linkListe.getSelectedIndex()!=-1){
			selectedID = IDs[fileListe.getSelectedIndex()][linkListe.getSelectedIndex()];
		}
		dispose();
	}

	/**
	 * Bei "Abbrechen" wird der Dialog nur geschlossen.
	 */
	public void cancelAction(){
		dispose();
	}

	/**
	 * Diese Klasse ist der Liste mit den Dateinamen zugeordnet.
	 * @author fechner
	 *
	 */
	class fileListListener implements ItemListener {

		/**
		 * Wenn ein neuer Eintrag in der Liste ausgew�hlt wurde, wird
		 * diese Methode aufgerufen.
		 */
		@Override
		public void itemStateChanged(ItemEvent e) {
			// Die Liste und der gew�hlte Eintrag werden gelesen, ..
			List l = (List)e.getSource();
			int auswahl = l.getSelectedIndex();
			// .. aus der unteren Liste werden alle Eintr�ge entfernt, ..
			linkListe.removeAll();
			// .. und die zur gew�hlten Datei geh�rigen Verweisziele eingef�gt.
			for (int j=0; j<links[auswahl].length; j++) {
				linkListe.add(links[auswahl][j]);
			}
		}

	}

	/**
	 * Gibt die ID des ausgew�hlten Verweises zur�ck, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgew�hlte ID
	 */
	public String getSelectedID() {
		return selectedID;
	}

	/**
	 * Gibt den Namen der ausgew�hlten Datei zur�ck, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Der ausgew�hlte Dateiname
	 */
	public String getSelectedFile() {
		return selectedFile;
	}

	/**
	 * Gibt die ID der ausgew�hlten Datei zur�ck, nachdem der Dialog mit "Ok" beendet wurde.
	 * @return Die ausgew�hlte Datei-ID
	 */
	public String getSelectedFileID() {
		return selectedFileID;
	}

}
