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
package org.columba.addressbook.folder.importfilter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.ByteBuffer;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.model.ContactModel;
import org.columba.addressbook.model.EmailModel;
import org.columba.addressbook.model.PhoneModel;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.ristretto.coder.Base64;

/**
 * Import addressbook data in LDIF format.
 */
public class NetscapeLDIFAddressbookImporter extends DefaultAddressbookImporter {
	public NetscapeLDIFAddressbookImporter() {
		super();
	}

	public NetscapeLDIFAddressbookImporter(File sourceFile,
			AbstractFolder destinationFolder) {
		super(sourceFile, destinationFolder);
	}

	public void importAddressbook(File file) throws Exception {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String str;
		ContactModel card = new ContactModel();

		while ((str = in.readLine()) != null) {
			// start parsing line by line
			if (str.length() == 0) {
				// empty line, means new contactcard
				saveContact(card);

				card = new ContactModel();
			} else {
				// parse key:value lines
				int index = str.indexOf(':');

				if ((index > 0) && (index < (str.length() - 1))) {
					String key = str.substring(0, index);
					String value;

					if (str.charAt(index + 1) == ':') {
						ByteBuffer bytes = Base64.decode(str.substring(
								index + 2).trim());
						value = new String(bytes.array(), "UTF-8");
					} else {
						value = str.substring(index + 1);
					}

					value = value.trim();

					if (key.equalsIgnoreCase("cn")) {
						card.setSortString(value);
					} else if (key.equalsIgnoreCase("givenname")) {
						card.setGivenName(value);
					} else if (key.equalsIgnoreCase("sn")) {
						card.setFamilyName(value);
					} else if (key.equalsIgnoreCase("mail")) {
						card.addEmail(new EmailModel(value,
								EmailModel.TYPE_WORK));
					} else if (key.equalsIgnoreCase("xmozillanickname")) {
						card.setNickName(value);
					} else if (key.equalsIgnoreCase("o")) {
						card.setOrganisation(value);
					} else if (key.equalsIgnoreCase("telephonenumber")) {
						card.addPhone(new PhoneModel(value,
								PhoneModel.TYPE_BUSINESS_PHONE));
					} else if (key.equalsIgnoreCase("homephone")) {
						card.addPhone(new PhoneModel(value,
								PhoneModel.TYPE_HOME_PHONE));
					} else if (key.equalsIgnoreCase("facsimiletelephonenumber")) {
						card.addPhone(new PhoneModel(value,
								PhoneModel.TYPE_BUSINESS_FAX));
					} else if (key.equalsIgnoreCase("pagerphone")) {
						card.addPhone(new PhoneModel(value,
								PhoneModel.TYPE_PAGER));
					} else if (key.equalsIgnoreCase("cellphone")) {
						card.addPhone(new PhoneModel(value,
								PhoneModel.TYPE_MOBILE_PHONE));
					} else if (key.equalsIgnoreCase("homeurl")) {
						card.setHomePage(value);
					}
				}
			}
		}

		in.close();
	}

	public String getDescription() {
		return AddressbookResourceLoader.getString("dialog",
				"addressbookimport", "netscapeldifaddressbook_description");
	}
}
