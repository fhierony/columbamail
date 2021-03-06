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
package org.columba.calendar.command;

import java.util.Calendar;

import javax.swing.JOptionPane;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.core.command.Command;
import org.columba.core.gui.frame.FrameManager;

public class ActivityMovedCommand extends Command {

	public ActivityMovedCommand(ICommandReference reference) {
		super(reference);
	}

	@Override
	public void execute(IWorkerStatusController worker) throws Exception {
		ICalendarStore store = ((CalendarCommandReference) getReference())
				.getStore();

		IActivity eventItem = ((CalendarCommandReference) getReference())
				.getActivity();

		try {
			IEventInfo model = (IEventInfo) store.get(eventItem.getId());

			Calendar start = eventItem.getDtStart();
			Calendar end = eventItem.getDtEnd();

			// update start/end time
			model.getEvent().setDtStart(start);
			model.getEvent().setDtEnd(end);

			// update store
			store.modify(eventItem.getId(), model);
		} catch (StoreException e) {
			JOptionPane.showMessageDialog(FrameManager.getInstance()
					.getActiveFrame(), e.getMessage());
			e.printStackTrace();
		}

	}

}
