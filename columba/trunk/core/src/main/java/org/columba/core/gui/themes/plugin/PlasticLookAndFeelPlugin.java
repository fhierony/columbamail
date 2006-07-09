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
package org.columba.core.gui.themes.plugin;

import java.awt.Dimension;
import java.util.List;

import org.columba.core.config.Config;
import org.columba.core.xml.XmlElement;

import com.jgoodies.looks.LookUtils;
import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PlasticLookAndFeel;
import com.jgoodies.looks.plastic.PlasticTheme;
import com.jgoodies.looks.plastic.PlasticXPLookAndFeel;

/**
 * JGoodies Looks L&F plugin
 * @author frd
 */
public class PlasticLookAndFeelPlugin extends AbstractThemePlugin {
	/**
	 * 
	 */
	public PlasticLookAndFeelPlugin() {
		super();
	}

	/**
	 * @see org.columba.core.gui.themes.plugin.AbstractThemePlugin#setLookAndFeel()
	 */
	public void setLookAndFeel() throws Exception {

		Options.setDefaultIconSize(new Dimension(16, 16));

		Options.setPopupDropShadowEnabled(true);
		
		XmlElement options = Config.getInstance().get("options").getElement(
				"/options");
		XmlElement gui = options.getElement("gui");
		XmlElement themeElement = gui.getElement("theme");

		try {
			// UIManager.setLookAndFeel(lafName);
			String theme = themeElement.getAttribute("theme");

			if (theme != null) {
				PlasticTheme t = getTheme(theme);
				LookUtils.setLookAndTheme(new PlasticXPLookAndFeel(), t);
			} else {
				PlasticTheme t = PlasticLookAndFeel.createMyDefaultTheme();
				LookUtils.setLookAndTheme(new PlasticXPLookAndFeel(), t);
			}

		} catch (Exception e) {
			System.err.println("Can't set look & feel:" + e);
		}

		;
	}

	protected PlasticTheme[] computeThemes() {
		List themes = PlasticLookAndFeel.getInstalledThemes();

		return (PlasticTheme[]) themes.toArray(new PlasticTheme[themes.size()]);
	}

	protected PlasticTheme getTheme(String name) {
		PlasticTheme[] themes = computeThemes();

		for (int i = 0; i < themes.length; i++) {
			String str = themes[i].getName();

			if (name.equals(str)) {
				return themes[i];
			}
		}

		return null;
	}
}
