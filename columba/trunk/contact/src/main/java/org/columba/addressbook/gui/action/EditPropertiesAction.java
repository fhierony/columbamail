// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.AddressbookTreeNode;
import org.columba.addressbook.folder.GroupFolder;
import org.columba.addressbook.folder.IContactStorage;
import org.columba.addressbook.folder.IGroupFolder;
import org.columba.addressbook.gui.dialog.contact.ContactEditorDialog;
import org.columba.addressbook.gui.dialog.group.EditGroupDialog;
import org.columba.addressbook.gui.focus.FocusManager;
import org.columba.addressbook.gui.focus.FocusOwner;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.table.TableController;
import org.columba.addressbook.gui.tree.TreeController;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.IGroupModel;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.dialog.ErrorDialog;
import org.columba.core.logging.Logging;

/**
 * Edit properties of selected contact or group.
 * 
 * @author fdietz
 */
public class EditPropertiesAction extends DefaultTableAction implements
		TreeSelectionListener {
	public EditPropertiesAction(IFrameMediator frameController) {
		super(frameController, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_properties"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_properties_tooltip")
				.replaceAll("&", ""));

		putValue(TOOLBAR_NAME, AddressbookResourceLoader.getString("menu",
				"mainframe", "menu_file_properties_toolbar"));

		

		setEnabled(false);

		//		 register interest on tree selection changes
		((AddressbookFrameMediator) frameMediator)
				.addTreeSelectionListener(this);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		AddressbookFrameMediator mediator = (AddressbookFrameMediator) frameMediator;

		FocusOwner focusOwner = FocusManager.getInstance().getCurrentOwner();

		TableController table = ((AddressbookFrameMediator) frameMediator)
				.getTable();

		boolean tableHasFocus = false;
		if (table.equals(focusOwner))
			tableHasFocus = true;

		if (tableHasFocus) {

			// get selected contact/group card
			String[] uids = mediator.getTable().getUids();

			// get selected folder
			IContactStorage folder = (IContactStorage) mediator.getTree()
					.getSelectedFolder();

			if (uids.length == 0) {
				return;
			}

			IContactModel card = null;
			try {
				card = (IContactModel) folder.get(uids[0]);
			} catch (Exception e) {

				if (Logging.DEBUG)
					e.printStackTrace();

				ErrorDialog.createDialog(e.getMessage(), e);
			}

			// 
			ContactEditorDialog dialog = new ContactEditorDialog(mediator.getView()
					.getFrame(), card);

			if (dialog.getResult()) {

				try {
					// modify card properties in folder
					folder.modify(uids[0], dialog.getDestModel());
				} catch (Exception e1) {
					if (Logging.DEBUG)
						e1.printStackTrace();

					ErrorDialog.createDialog(e1.getMessage(), e1);
				}

				if (folder instanceof GroupFolder)
					//					 re-select folder
					mediator.getTree().setSelectedFolder(
							(AbstractFolder) folder);

			}

		} else {
			// tree has focus

			GroupFolder folder = (GroupFolder) mediator.getTree()
					.getSelectedFolder();

			IGroupModel card = folder.getGroup();

			EditGroupDialog dialog = new EditGroupDialog(mediator.getView()
					.getFrame(), card, (AbstractFolder) folder.getParent());

			if (dialog.getResult()) {
				// re-select folder
				mediator.getTree().setSelectedFolder(folder);

			}

		}

	}

	/**
	 * Enable or disable action on selection change.
	 * 
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent event) {
		// return if selection change is in flux
		if (event.getValueIsAdjusting()) {
			return;
		}

		FocusOwner focusOwner = FocusManager.getInstance().getCurrentOwner();

		TableController table = ((AddressbookFrameMediator) frameMediator)
				.getTable();

		if (table.equals(focusOwner)) {

			// table has focus

			Object[] uids = ((AddressbookFrameMediator) frameMediator)
					.getTable().getUids();

			if (uids.length > 0) {
				setEnabled(true);
				return;
			}
		}

		setEnabled(false);

	}

	public void valueChanged(TreeSelectionEvent e) {
		TreePath path = e.getNewLeadSelectionPath();

		FocusOwner focusOwner = FocusManager.getInstance().getCurrentOwner();

		TreeController tree = ((AddressbookFrameMediator) frameMediator)
				.getTree();

		if (tree.equals(focusOwner)) {
			// tree has focus

			AddressbookTreeNode treeNode = null;
			// remember last selected folder treenode
			if (path != null) {
				treeNode = (AddressbookTreeNode) path.getLastPathComponent();
			}

			// enable, if more than zero treenodes selected
			if ((path != null) && (treeNode instanceof IGroupFolder)) {
				setEnabled(true);
			} else
				setEnabled(false);

		} else

			setEnabled(false);
	}
}