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
package org.columba.addressbook.gui.table;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.columba.addressbook.facade.IContactItem;
import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.AddressbookTreeNode;
import org.columba.addressbook.folder.FolderListener;
import org.columba.addressbook.folder.IContactStorage;
import org.columba.addressbook.folder.IFolderEvent;
import org.columba.addressbook.gui.focus.FocusManager;
import org.columba.addressbook.gui.focus.FocusOwner;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.gui.table.model.AddressbookTableModel;
import org.columba.addressbook.gui.table.model.FilterDecorator;
import org.columba.addressbook.gui.table.model.SortDecorator;
import org.columba.addressbook.gui.tree.TreeView;
import org.columba.addressbook.model.IContactModelPartial;
import org.columba.core.gui.dialog.ErrorDialog;
import org.columba.core.logging.Logging;

/**
 * @author fdietz
 */
public class TableController implements TreeSelectionListener, FolderListener,
		FocusOwner {

	private TableView view;

	private AddressbookFrameMediator mediator;

	private AddressbookTableModel addressbookModel;

	private SortDecorator sortDecorator;

	private FilterDecorator filterDecorator;

	private AddressbookTreeNode selectedFolder;

	/**
	 * 
	 */
	public TableController(AddressbookFrameMediator mediator) {
		super();

		this.mediator = mediator;

		addressbookModel = new AddressbookTableModel();

		filterDecorator = new FilterDecorator(addressbookModel);

		sortDecorator = new SortDecorator(filterDecorator);

		view = new TableView(this, sortDecorator);

		addMouseListenerToHeaderInTable();

		view.addMouseListener(new TableMouseListener(this));

		// register as focus owner
		FocusManager.getInstance().registerComponent(this);

		selectedFolder = null;
	}

	/**
	 * Add MouseListener to JTableHeader to sort table based on clicked column
	 * header.
	 * 
	 */
	protected void addMouseListenerToHeaderInTable() {
		final JTable tableView = getView();

		tableView.setColumnSelectionAllowed(false);

		MouseAdapter listMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				TableColumnModel columnModel = tableView.getColumnModel();
				int viewColumn = columnModel.getColumnIndexAtX(e.getX());
				int column = tableView.convertColumnIndexToModel(viewColumn);

				if ((e.getClickCount() == 1) && (column != -1)) {

					if (sortDecorator.getSelectedColumn() == column)
						sortDecorator
								.setSortOrder(!sortDecorator.isSortOrder());

					String[] uids = getUids();

					sortDecorator.sort(column);

					setSelectedItems(uids);
				}
			}
		};

		JTableHeader th = tableView.getTableHeader();
		th.addMouseListener(listMouseListener);
	}

	/**
	 * @return AddressbookFrameController
	 */
	public AddressbookFrameMediator getMediator() {
		return mediator;
	}

	/**
	 * @return TableView
	 */
	public TableView getView() {
		return view;
	}

	public void setSelectedFolder(IContactStorage store) {
		sortDecorator.setContactItemMap(((AbstractFolder) store)
				.getContactItemMap());
	}

	/**
	 * Update table on tree selection changes.
	 */
	public void valueChanged(TreeSelectionEvent e) {
		if (!(e.getSource() instanceof TreeView))
				return;

		if (selectedFolder != null) {
			((AbstractFolder) selectedFolder).removeFolderListener(this);
		}

		TreeView treeview = (TreeView)e.getSource();

		if ( treeview.getSelectionPath() != null) {
			AddressbookTreeNode node = (AddressbookTreeNode) treeview.getSelectionPath()
					.getLastPathComponent();

			if (node instanceof IContactStorage) {
				try {
					((AbstractFolder) node).addFolderListener(this);
	
					selectedFolder = node;
	
					sortDecorator
							.setContactItemMap(((AbstractFolder) selectedFolder)
									.getContactItemMap());
	
					return;
				} catch (Exception e1) {
					if (Logging.DEBUG)
						e1.printStackTrace();
	
					ErrorDialog.createDialog(e1.getMessage(), e1);
				}
			}
		}

		sortDecorator.setContactItemMap(null);
		selectedFolder = null;
	}

	/**
	 * Get selected uids.
	 * 
	 * @return
	 */
	public String[] getUids() {
		int[] rows = getView().getSelectedRows();
		String[] uids = new String[rows.length];

		IContactModelPartial item;

		for (int i = 0; i < rows.length; i++) {
			item = (IContactModelPartial) sortDecorator
					.getContactItem(rows[i]);
			uids[i] = item.getId();
		}

		return uids;
	}

	/**
	 * @return Returns the addressbookModel.
	 */
	public AddressbookTableModel getAddressbookModel() {
		return addressbookModel;
	}

	public IContactItem getSelectedItem() {
		int row = getView().getSelectedRow();
		if (row == -1)
			return null;

		// we use the SortDecorator, because the indices are sorted
		IContactItem item = (IContactItem) sortDecorator.getContactItem(row);

		return item;
	}

	public void setSelectedItems(String[] uids) {
		IContactModelPartial item;
		List<String> l = Arrays.asList(uids);

		for (int i = 0; i < sortDecorator.getRowCount(); i++) {
			item = (IContactModelPartial) sortDecorator
					.getContactItem(i);
			if (l.contains(item.getId()))
				view.addRowSelectionInterval(i, i);
		}
	}

	/** ********************** FolderListener ***************** */

	/**
	 * @see org.columba.addressbook.folder.FolderListener#itemAdded(org.columba.addressbook.folder.FolderEvent)
	 */
	public void itemAdded(IFolderEvent e) {
		getAddressbookModel().update();

	}

	/**
	 * @see org.columba.addressbook.folder.FolderListener#itemChanged(org.columba.addressbook.folder.FolderEvent)
	 */
	public void itemChanged(IFolderEvent e) {
		getAddressbookModel().update();

	}

	/**
	 * @see org.columba.addressbook.folder.FolderListener#itemRemoved(org.columba.addressbook.folder.FolderEvent)
	 */
	public void itemRemoved(IFolderEvent e) {
		getAddressbookModel().update();

	}

	/** ************* FocusOwner Implementation ****************** */

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#copy()
	 */
	public void copy() {

	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#cut()
	 */
	public void cut() {

	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#delete()
	 */
	public void delete() {

	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#getComponent()
	 */
	public JComponent getComponent() {
		return getView();
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isCopyActionEnabled()
	 */
	public boolean isCopyActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isCutActionEnabled()
	 */
	public boolean isCutActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isDeleteActionEnabled()
	 */
	public boolean isDeleteActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isPasteActionEnabled()
	 */
	public boolean isPasteActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isRedoActionEnabled()
	 */
	public boolean isRedoActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isSelectAllActionEnabled()
	 */
	public boolean isSelectAllActionEnabled() {
		if (getView().getRowCount() > 0) {
			return true;
		}

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#isUndoActionEnabled()
	 */
	public boolean isUndoActionEnabled() {

		return false;
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#paste()
	 */
	public void paste() {

	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#redo()
	 */
	public void redo() {

	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#selectAll()
	 */
	public void selectAll() {
		getView().selectAll();
	}

	/**
	 * @see org.columba.addressbook.gui.focus.FocusOwner#undo()
	 */
	public void undo() {

	}

	/**
	 * @return Returns the sortDecorator.
	 */
	public SortDecorator getSortDecorator() {
		return sortDecorator;
	}

	/**
	 * @return Returns the filterDecorator.
	 */
	public FilterDecorator getFilterDecorator() {
		return filterDecorator;
	}

	
}