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
package org.columba.mail.gui.table.model;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.columba.mail.gui.table.IHeaderTableModel;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.ICloseableIterator;
import org.columba.mail.message.IColumbaHeader;
import org.columba.mail.message.IHeaderList;
import org.frapuccino.treetable.AbstractTreeTableModel;
import org.frapuccino.treetable.CustomTreeTableCellRenderer;

public class HeaderTableModel extends AbstractTreeTableModel implements IHeaderTableModel {

	/**
	 * list of column IDs
	 */
	private List columns = new Vector();

	protected IHeaderList headerList;

	/**
	 * 
	 * We cache all <class>MessageNode </class> here.
	 * 
	 * This is much faster than searching through the complete <class>HeaderList
	 * </class> all the time.
	 *  
	 */
	protected Map map = new HashMap();

	protected MessageNode root;

	private boolean enableThreadedView;
	
	private Vector visitors = new Vector();

	public HeaderTableModel() {
	}

	public HeaderTableModel(String[] c) {
		// add array to vector
		for (int i = 0; i < c.length; i++) {
			columns.add(c[i]);
		}
	}
	
	public void registerVisitor(ModelVisitor visitor) {
		visitors.add(visitor);
	}

	/**
	 * ***************************** implements TableModelModifier
	 * ******************
	 */

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.table.model.TableModelModifier#modify(java.lang.Object[])
	 */
	public void modify(Object[] uids) {
		for (int i = 0; i < uids.length; i++) {
			MessageNode node = (MessageNode) map.get(uids[i]);

			IColumbaHeader header = getHeaderList().get(uids[i]);
			if( header == null) continue;
			
			node.setUserObject(header);
			
			if (node != null) {
				// update treemodel
				getTreeModel().nodeChanged(node);
			}
		}

	}

	private void removeFromMap(MessageNode node) {
		for (int i = 0; i < node.getChildCount(); i++)
			removeFromMap((MessageNode)node.getChildAt(i));

		map.remove(node.getUid());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.table.model.TableModelModifier#remove(java.lang.Object[])
	 */
	public void remove(Object[] uids) {
		if (uids != null) {
			for (int i = 0; i < uids.length; i++) {
				MessageNode node = (MessageNode) map.get(uids[i]);

				if (node != null) {
					removeFromMap(node);

					if (node.getParent() != null) {
						getTreeModel().removeNodeFromParent(node);
					}
				}
			}
		}

		update();
	}

	public void reset() {
		if (root == null) {
			root = new MessageNode(new ColumbaHeader(), "0");

			tree.setRootNode(root);		
		}

		// remove all children from tree
		root.removeAllChildren();

		// clear messagenode cache
		map.clear();
		
		if ((headerList == null) || (headerList.count() == 0)) {
			// table is empty
			// -> just display empty table
			
			getTreeModel().nodeStructureChanged(getRootNode());
			
			fireTableDataChanged();
			
			return;
		}
		
		
	}

	public void expandUnseen(MessageNode node) {
		if (node == null)
			return;
		for (int i = 0; i < node.getChildCount(); i++) {
			expandUnseen((MessageNode)node.getChildAt(i));
		}

		if (!node.getHeader().getFlags().getSeen()) {
			TreePath path = new TreePath(node.getPath()).getParentPath();
			getTree().expandPath(path);
		}
	}

	public void update() {
		if ((headerList == null) || (headerList.count() == 0)) {
			// table is empty
			// -> just display empty table			
			root.removeAllChildren();
			
			getTreeModel().nodeStructureChanged(getRootNode());
			
			fireTableDataChanged();
			
			return;
		}
		
		// add every header from HeaderList to the table as MessageNode
		ICloseableIterator it;
		for (it = headerList.keyIterator(); it.hasNext();) {
			// get unique id
			Object uid = it.next();
			
			if( !map.containsKey(uid)) {
				// get header
				IColumbaHeader header = headerList.get(uid);

				// create MessageNode
				MessageNode child = new MessageNode(header, uid);

				// add this node to cache
				map.put(uid, child);

				// add node to tree
				root.add(child);
			}
		}
		it.close();

		
		Enumeration e = visitors.elements();
		while (e.hasMoreElements()) {
			ModelVisitor v = (ModelVisitor) e.nextElement();
			v.visit(this);
		}
		
		getTreeModel().nodeStructureChanged(getRootNode());
		
		if (enableThreadedView)
			expandUnseen(root);
		
		fireTableDataChanged();
	}
	
	public void clear() {
		root = new MessageNode(new ColumbaHeader(), "0");
		tree.setRootNode(root);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.mail.gui.table.model.TableModelModifier#set(org.columba.mail.message.HeaderList)
	 */
	public void set(IHeaderList headerList) {
		this.headerList = headerList;

		reset();
		update();
	}

	/** ********************** getter/setter methods *************************** */
	public void enableThreadedView(boolean b) {
		enableThreadedView = b;
	}

	public MessageNode getRootNode() {
		return root;
	}

	public IHeaderList getHeaderList() {
		return headerList;
	}

	public MessageNode getMessageNode(Object uid) {
		return (MessageNode) map.get(uid);
	}

	public DefaultTreeModel getTreeModel() {
		return (DefaultTreeModel) getTree().getModel();
	}

	/** ******************* AbstractTableModel implementation ******************* */
	public int getColumnCount() {
		return columns.size();
	}

	public int getRowCount() {
		if (getTree() != null) {
			return getTree().getRowCount();
		} else {
			return 0;
		}
	}

	public Object getValueAt(int row, int col) {
		//if ( col == 0 ) return tree;
		TreePath treePath = getTree().getPathForRow(row);
		if( treePath == null) return null;
		
		return (MessageNode) treePath.getLastPathComponent();
	}

	/**
	 * Get row for node.
	 * 
	 * @param node
	 *            selected message node
	 * @return current row of node
	 */
	public int getRow(MessageNode node) {

		for (int i = 0; i < getTree().getRowCount(); i++) {
			MessageNode n = (MessageNode) getValueAt(i, 0);
			if (n.getUid().equals(node.getUid()))
				return i;
		}

		return -1;
	}

	public String getColumnName(int column) {
		return (String) columns.get(column);
	}

	public int getColumnNumber(String name) {
		for (int i = 0; i < getColumnCount(); i++) {
			if (name.equals(getColumnName(i))) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Get the class which is responsible for renderering this column.
	 * <p>
	 * If the threaded-view is enabled, return a custom tree cell renderer.
	 * <p>
	 * 
	 * @see org.columba.mail.gui.table.TableView#enableThreadedView
	 */
	public Class getColumnClass(int column) {
		if (enableThreadedView) {
			if (getColumnName(column).equals("Subject")) {
				return CustomTreeTableCellRenderer.class;
			}
		}

		return getValueAt(0, column).getClass();
	}

	public boolean isCellEditable(int row, int col) {
		String name = getColumnName(col);

		if (name.equalsIgnoreCase("Subject")) {
			return true;
		}

		return false;
	}

	/**
	 * Set column IDs
	 * 
	 * @param c
	 *            array of column IDs
	 */
	public void setColumns(String[] c) {
		columns = new Vector();

		// add array to vector
		for (int i = 0; i < c.length; i++) {
			columns.add(c[i]);
		}
	}

	/**
	 * Add column to table model.
	 * 
	 * @param c
	 *            new column ID
	 */
	public void addColumn(String c) {
		columns.add(c);
	}

	/**
	 * Clear column list.
	 *  
	 */
	public void clearColumns() {
		columns.clear();
	}

	/**
	 * @see org.columba.mail.gui.table.IHeaderTableModel#getMessageNodeAtRow(int)
	 */
	public MessageNode getMessageNodeAtRow(int index) {
		return (MessageNode) getValueAt(index,0);
	}

	public void columnMarginChanged(ChangeEvent e) {
	}

	public void columnSelectionChanged(ListSelectionEvent e) {
	}

	public void columnAdded(TableColumnModelEvent e) {
	}

	public void columnMoved(TableColumnModelEvent e) {
		if( e.getFromIndex() != e.getToIndex()) {
			columns.add(e.getToIndex(),columns.remove(e.getFromIndex()));
		}
	}

	public void columnRemoved(TableColumnModelEvent e) {
	}

}