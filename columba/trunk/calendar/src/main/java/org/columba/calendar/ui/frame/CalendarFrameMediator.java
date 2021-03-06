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
package org.columba.calendar.ui.frame;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import org.columba.api.gui.frame.IContainer;
import org.columba.api.gui.frame.IDock;
import org.columba.api.gui.frame.IDockable;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.config.CalendarList;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IComponentInfo;
import org.columba.calendar.model.api.IDateRange;
import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.resourceloader.ResourceLoader;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.ui.action.ActivityMovedAction;
import org.columba.calendar.ui.action.EditActivityAction;
import org.columba.calendar.ui.action.NewAppointmentAction;
import org.columba.calendar.ui.calendar.MainCalendarController;
import org.columba.calendar.ui.calendar.api.ActivitySelectionChangedEvent;
import org.columba.calendar.ui.calendar.api.IActivitySelectionChangedListener;
import org.columba.calendar.ui.calendar.api.ICalendarView;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.calendar.ui.list.CalendarListController;
import org.columba.calendar.ui.list.api.ICalendarListView;
import org.columba.calendar.ui.navigation.NavigationController;
import org.columba.calendar.ui.navigation.api.DateRangeChangedEvent;
import org.columba.calendar.ui.navigation.api.ICalendarNavigationView;
import org.columba.calendar.ui.navigation.api.IDateRangeChangedListener;
import org.columba.calendar.ui.tagging.CalendarTagList;
import org.columba.core.config.ViewItem;
import org.columba.core.context.base.api.IStructureValue;
import org.columba.core.context.semantic.api.ISemanticContext;
import org.columba.core.gui.frame.DockFrameController;
import org.columba.core.gui.tagging.TagList;
import org.columba.core.gui.tagging.TagPopupMenu;

/**
 * @author fdietz
 * 
 */
public class CalendarFrameMediator extends DockFrameController implements
		ICalendarMediator {

	public static final String PROP_FILTERED = "filterRow";

	private CalendarListController listController;

	public MainCalendarController calendarController;

	private ICalendarNavigationView navigationController;

	private IDockable listPanel;

	private IDockable calendarPanel;

	private IDockable navigationPanel;

	private IDockable tagListDockable;

	/**
	 * @param viewItem
	 */
	public CalendarFrameMediator(ViewItem viewItem) {
		super(viewItem);

		// TestDataGenerator.generateTestData();

		calendarController = new MainCalendarController(this);

		calendarController
				.addSelectionChangedListener(new MyActivitySelectionChangedListener());

		navigationController = new NavigationController();

		navigationController
				.addSelectionChangedListener(new IDateRangeChangedListener() {
					public void selectionChanged(DateRangeChangedEvent object) {
						calendarController.setVisibleDateRange(object
								.getDateRange());

					}
				});

		listController = new CalendarListController(this);
		listController.getView().addMouseListener(new ListMouseListener());

		registerDockables();

		initContextMenu();

	}

	private void initContextMenu() {
		listController.createPopupMenu(this);
		calendarController.createPopupMenu(this);
	}

	private void registerDockables() {

		// calendar list
		JScrollPane treeScrollPane = new JScrollPane(listController.getView());
		// set background of scrollpane, in case the list is smaller than the
		// dockable
		treeScrollPane.getViewport().setBackground(
				UIManager.getColor("List.background"));
		treeScrollPane.setBackground(UIManager.getColor("List.background"));
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		listPanel = registerDockable("calendar_tree", ResourceLoader.getString(
				"global", "dockable_calendarlist"), treeScrollPane, null);

		// JScrollPane tableScrollPane = new JScrollPane(navigationController
		// .getView());
		// tableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0,
		// 0));

		navigationPanel = registerDockable("navigation", ResourceLoader
				.getString("global", "dockable_navigation"),
				navigationController.getView(), null);

		calendarPanel = registerDockable("main_calendar", ResourceLoader
				.getString("global", "dockable_maincalendar"),
				calendarController.getView(), null);

//		TagList tagList = new CalendarTagList(this);
//		JScrollPane tagListScrollPane = new JScrollPane(tagList);
//		tagListScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		tagListScrollPane
//				.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

//		tagListDockable = registerDockable("calendar_taglist", ResourceLoader
//				.getString("global", "dockable_taglist"), tagListScrollPane,
//				new TagPopupMenu(this, tagList));
//		tagList.setPopupMenu(new TagPopupMenu(this, tagList));

	}

	/**
	 * @see org.columba.core.gui.frame.DockFrameController#loadDefaultPosition()
	 */
	public void loadDefaultPosition() {

		super.dock(calendarPanel, IDock.REGION.CENTER);
		super.dock(listPanel, calendarPanel, IDock.REGION.WEST, 0.2f);
		super.dock(navigationPanel, listPanel, IDock.REGION.SOUTH, 0.2f);

		super.setSplitProportion(listPanel, 0.2f);
		super.setSplitProportion(calendarPanel, 0.2f);
	}

	/** *********************** container callbacks ************* */

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#extendMenu(org.columba.api.gui.frame.IContainer)
	 */
	public void extendMenu(IContainer container) {
		InputStream is = this.getClass().getResourceAsStream(
				"/org/columba/calendar/action/menu.xml");
		getContainer().extendMenu(this, is);
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#extendToolBar(org.columba.api.gui.frame.IContainer)
	 */
	public void extendToolBar(IContainer container) {

		InputStream is2 = this.getClass().getResourceAsStream(
				"/org/columba/calendar/action/toolbar.xml");
		getContainer().extendToolbar(this, is2);
	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#showDayView()
	 */
	public void showDayView() {

		calendarController.setViewMode(ICalendarView.VIEW_MODE_DAY);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_DAY);

	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#showWeekView()
	 */
	public void showWeekView() {
		calendarController.setViewMode(ICalendarView.VIEW_MODE_WEEK);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_WEEK);

	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#showWorkWeekView()
	 */
	public void showWorkWeekView() {
		calendarController.setViewMode(ICalendarView.VIEW_MODE_WORK_WEEK);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_WORK_WEEK);

	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#showMonthView()
	 */
	public void showMonthView() {
		calendarController.setViewMode(ICalendarView.VIEW_MODE_MONTH);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_MONTH);

	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#getCalendarView()
	 */
	public ICalendarView getCalendarView() {
		return calendarController;
	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#getListView()
	 */
	public ICalendarListView getListView() {
		return listController;
	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#getNavigationView()
	 */
	public ICalendarNavigationView getNavigationView() {
		return navigationController;
	}

	/**
	 * Double-click mouse listener for calendar list component.
	 */
	class ListMouseListener extends MouseAdapter {

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		public void mousePressed(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		public void mouseReleased(MouseEvent event) {
			if (event.isPopupTrigger()) {
				processPopup(event);
			}
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		public void mouseClicked(MouseEvent event) {
			// if mouse button was pressed twice times
			if (event.getClickCount() == 2) {

			}
		}

		protected void processPopup(final MouseEvent event) {

			listController.getPopupMenu().show(event.getComponent(),
					event.getX(), event.getY());

		}
	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#fireFilterUpdated()
	 */
	public void fireFilterUpdated() {
		calendarController.recreateFilterRows();

	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#fireActivityMoved(org.columba.calendar.base.api.IActivity)
	 */
	public void fireActivityMoved(IActivity activity) {
		new ActivityMovedAction(this).actionPerformed(null);
	}

	/**
	 * @see org.columba.calendar.ui.frame.api.ICalendarMediator#fireStartActivityEditing(org.columba.calendar.base.api.IActivity)
	 */
	public void fireStartActivityEditing(IActivity activity) {
		new EditActivityAction(this).actionPerformed(null);
	}

	public void fireCreateActivity(IDateRange range) {
		new NewAppointmentAction(this, range).actionPerformed(null);
	}

	/**
	 * @see org.columba.core.gui.frame.DefaultFrameController#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	@Override
	public String getString(String sPath, String sName, String sID) {
		return ResourceLoader.getString(sPath, sID);
	}

	class MyActivitySelectionChangedListener implements
			IActivitySelectionChangedListener {

		MyActivitySelectionChangedListener() {
		}

		public void selectionChanged(ActivitySelectionChangedEvent object) {
			IActivity[] selection = object.getSelection();
			if (selection == null)
				return;

			if (selection.length == 1) {

				ICalendarStore store = CalendarList.getInstance().get(selection[0].getCalendarId()).getStore();
				if (store == null)
					return;

				IComponentInfo c = store.get(selection[0].getId());

				if (c.getType().equals(IComponent.TYPE.EVENT)) {
					IEventInfo event = (IEventInfo) c;

					String summary = event.getEvent().getSummary();
					String description = event.getEvent().getDescription();
					String location = event.getEvent().getLocation();
					Calendar dtStart = event.getEvent().getDtStart();
					Calendar dtEnd = event.getEvent().getDtEnd();

					// create empty value
					IStructureValue value = getSemanticContext().createValue();

					// create identity value
					IStructureValue eventValue = value.addChild(
							ISemanticContext.CONTEXT_NODE_EVENT,
							ISemanticContext.CONTEXT_NAMESPACE_CORE);

					// summary
					eventValue.setString(ISemanticContext.CONTEXT_ATTR_SUMMARY,
							ISemanticContext.CONTEXT_NAMESPACE_CORE, summary);
					// description
					eventValue.setString(
							ISemanticContext.CONTEXT_ATTR_DESCRIPTION,
							ISemanticContext.CONTEXT_NAMESPACE_CORE,
							description);
					// location
					eventValue.setString(
							ISemanticContext.CONTEXT_ATTR_LOCATION,
							ISemanticContext.CONTEXT_NAMESPACE_CORE, location);

					// date range
					IStructureValue dateRangeValue = eventValue.addChild(
							ISemanticContext.CONTEXT_NODE_DATERANGE,
							ISemanticContext.CONTEXT_NAMESPACE_CORE);
					dateRangeValue.setDate(
							ISemanticContext.CONTEXT_ATTR_STARTDATE,
							ISemanticContext.CONTEXT_NAMESPACE_CORE, dtStart.getTime());
					dateRangeValue.setDate(
							ISemanticContext.CONTEXT_ATTR_ENDDATE,
							ISemanticContext.CONTEXT_NAMESPACE_CORE, dtEnd.getTime());
				}
			}
		}

	}

}