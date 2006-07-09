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
package org.columba.calendar.store;

import java.util.Comparator;

import org.columba.calendar.store.api.StoreEvent;

class StoreEventComparator implements Comparator {

	private static StoreEventComparator instance = new StoreEventComparator();

	private StoreEventComparator() {
	}

	public static StoreEventComparator getInstance() {
		return instance;
	}

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object arg0, Object arg1) {

		StoreEvent a = (StoreEvent) arg0;
		StoreEvent b = (StoreEvent) arg1;

		return (a.getSource() != b.getSource()) ? 0 : 1;
	}

}