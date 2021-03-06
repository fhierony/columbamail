//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.folder.virtual;

import java.text.MessageFormat;

import org.columba.api.command.ICommandReference;
import org.columba.api.command.IWorkerStatusController;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.connectionstate.ConnectionStateImpl;
import org.columba.core.folder.api.IFolderCommandReference;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.folder.AbstractRemoteFolder;
import org.columba.mail.folder.FolderChildrenIterator;
import org.columba.mail.folder.IMailFolder;
import org.columba.mail.util.MailResourceLoader;

public class ActivateVirtualFolderCommand extends Command {

	public ActivateVirtualFolderCommand(ICommandReference reference) {
		super(reference);
	}

	public void execute(IWorkerStatusController worker) throws Exception {
		VirtualFolder vFolder = (VirtualFolder) ((IFolderCommandReference) getReference())
				.getSourceFolder();

		worker.setDisplayText(MessageFormat.format(MailResourceLoader
				.getString("statusbar", "message", "activate_vfolder"),
				new Object[] { vFolder.getName() }));

		vFolder.activate();
	}

	public static void activateAll(IMailFolder root) {
		// Find all VirtualFolders and rewrite the FolderReference
		FolderChildrenIterator it = new FolderChildrenIterator(root);

		while (it.hasMoreChildren()) {
			IMailFolder f = it.nextChild();
			if (f instanceof VirtualFolder && !f.getId().equals("106") && !((IMailFolder)f.getParent()).getId().equals("106")) {
				VirtualFolder vFolder = (VirtualFolder)f;
				
				// Check if the parentfolder is remote & we are online				
				if( vFolder.getSourceFolder() instanceof AbstractRemoteFolder && !ConnectionStateImpl.getInstance().isOnline()) {
					continue;
				}
					
				CommandProcessor.getInstance().addOp(
						new ActivateVirtualFolderCommand(
								new MailFolderCommandReference(f)));
			}
		}

	}

}
