/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Ristretto Mail API.
 *
 * The Initial Developers of the Original Code are
 * Timo Stich and Frederik Dietz.
 * Portions created by the Initial Developers are Copyright (C) 2004
 * All Rights Reserved.
 *
 * Contributor(s):
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package org.columba.ristretto.imap.parser;

import junit.framework.TestCase;

import org.columba.ristretto.imap.IMAPResponse;
import org.columba.ristretto.imap.MailboxStatus;

public class MailboxStatusParserTest extends TestCase {
    
    public void testStatus1() throws Exception {
        IMAPResponse response = IMAPResponseParser.parse("* STATUS blurdybloop (MESSAGES 231 UIDNEXT 44292)\r\n");
        MailboxStatus status = MailboxStatusParser.parse(response);
        
        assertEquals("blurdybloop", status.getName());
        assertEquals(231, status.getMessages());
        assertEquals(44292, status.getUidNext());
    }

    public void testStatus2() throws Exception {
        IMAPResponse response = IMAPResponseParser.parse("* STATUS \"blurdy\"oop\" (MESSAGES 231 UIDNEXT 44292)\r\n");
        MailboxStatus status = MailboxStatusParser.parse(response);
        
        assertEquals("blurdy\"oop", status.getName());
        assertEquals(231, status.getMessages());
        assertEquals(44292, status.getUidNext());
    }

    public void testStatus3() throws Exception {
        IMAPResponse response = IMAPResponseParser.parse("* STATUS blurdybloop (RECENT 231 UNSEEN 2 UIDVALIDITY 44292)\r\n");
        MailboxStatus status = MailboxStatusParser.parse(response);
        
        assertEquals("blurdybloop", status.getName());
        assertEquals(2, status.getUnseen());
        assertEquals(231, status.getRecent());
        assertEquals(44292, status.getUidValidity());
    }
}