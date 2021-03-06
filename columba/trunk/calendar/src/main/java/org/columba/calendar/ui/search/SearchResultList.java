package org.columba.calendar.ui.search;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.ListCellRenderer;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;

import org.columba.calendar.facade.DialogFacade;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.model.api.ITodo;
import org.columba.calendar.resourceloader.IconKeys;
import org.columba.calendar.resourceloader.ResourceLoader;
import org.columba.calendar.search.CalendarSearchResult;
import org.columba.core.gui.base.DoubleClickListener;
import org.columba.core.search.api.ISearchResult;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.decorator.Highlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.jdesktop.swingx.decorator.RolloverHighlighter;

public class SearchResultList extends JXList {

	private DefaultListModel listModel;

	private JPopupMenu contextMenu;

	public SearchResultList() {
		super();

		listModel = new DefaultListModel();
		setModel(listModel);
		setCellRenderer(new MyListCellRenderer());

		setBorder(null);
		setHighlighters(new HighlighterPipeline(
				new Highlighter[] { new RolloverHighlighter(new Color(248, 248,
						248), Color.white) }));
		setRolloverEnabled(true);

		addMouseListener(new DoubleClickListener() {
			@Override
			public void doubleClick(MouseEvent event) {
				ISearchResult result = (ISearchResult) getSelectedValue();
				URI uri = result.getLocation();
				new DialogFacade().openEventEditorDialog(uri);
			}
		});

		add(getPopupMenu());
		addMouseListener(new MyMouseListener());
	}

	private JPopupMenu getPopupMenu() {
		if (contextMenu != null)
			return contextMenu;

		contextMenu = new JPopupMenu();

		JMenuItem item = new JMenuItem("Open..");
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				ISearchResult result = (ISearchResult) getSelectedValue();
				URI uri = result.getLocation();
				new DialogFacade().openEventEditorDialog(uri);
			}
		});

		contextMenu.add(item);
		return contextMenu;
	}

	public void addAll(List<ISearchResult> result) {
		Iterator<ISearchResult> it = result.iterator();
		while (it.hasNext()) {
			listModel.addElement(it.next());
		}
	}

	public void add(ISearchResult result) {
		listModel.addElement(result);
	}

	public void clear() {
		listModel.clear();
	}

	class MyListCellRenderer extends JPanel implements ListCellRenderer {

		private JLabel iconLabel = new JLabel();

		private JLabel titleLabel = new JLabel();

		private JLabel startDateLabel = new JLabel();

		private JLabel descriptionLabel = new JLabel();

		private JPanel centerPanel;

		private Border lineBorder = new HeaderSeparatorBorder(new Color(230,
				230, 230));

		private DateFormat df = DateFormat.getDateTimeInstance();

		MyListCellRenderer() {
			setLayout(new BorderLayout());

			centerPanel = new JPanel();
			centerPanel.setLayout(new BorderLayout());

			JPanel titlePanel = new JPanel();
			titlePanel.setLayout(new BorderLayout());
			titlePanel.add(titleLabel, BorderLayout.WEST);
			titlePanel.add(startDateLabel, BorderLayout.EAST);

			centerPanel.add(titlePanel, BorderLayout.NORTH);
			centerPanel.add(descriptionLabel, BorderLayout.CENTER);
			centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
			add(iconLabel, BorderLayout.WEST);
			add(centerPanel, BorderLayout.CENTER);

			descriptionLabel.setFont(descriptionLabel.getFont().deriveFont(
					Font.ITALIC));

			setBorder(BorderFactory.createCompoundBorder(lineBorder,
					BorderFactory.createEmptyBorder(2, 2, 2, 2)));
			iconLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 8));

			titlePanel.setOpaque(false);
			centerPanel.setOpaque(false);
			setOpaque(true);

		}

		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());

			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());

			}

			CalendarSearchResult result = (CalendarSearchResult) value;

			titleLabel.setText(result.getTitle());
			iconLabel.setIcon(ResourceLoader
					.getSmallIcon(IconKeys.NEW_APPOINTMENT));
			if (result.getDescription() != null
					&& result.getDescription().length() > 0)
				descriptionLabel.setText(result.getDescription());
			else
				descriptionLabel.setText("no Description specified");

			IComponent c = result.getModel();
			if (c.getType().equals(IComponent.TYPE.EVENT)) {
				IEvent event = (IEvent) c;
				Calendar startDate = event.getDtStart();

				startDateLabel.setText(df.format(startDate.getTime()));
			} else if (c.getType().equals(IComponent.TYPE.TODO)) {
				ITodo todo = (ITodo) c;
				Calendar startDate = todo.getDtStart();
				startDateLabel.setText(df.format(startDate.getTime()));
			} else
				throw new IllegalArgumentException(
						"unsupported component type " + c.getType());

			return this;
		}

	}

	class MyMouseListener extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			handleEvent(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			handlePopupEvent(e);
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			handlePopupEvent(e);
		}

		/**
		 * @param e
		 */
		private void handlePopupEvent(MouseEvent e) {
			Point p = e.getPoint();
			if (e.isPopupTrigger()) {
				// check if a single entry is selected
				if (getSelectedIndices().length <= 1) {
					// select new item
					int index = locationToIndex(p);
					setSelectedIndex(index);
				}
				// show context menu
				getPopupMenu().show(e.getComponent(), p.x, p.y);
			}
		}

		/**
		 * @param e
		 */
		private void handleEvent(MouseEvent e) {
		}
	}

	class HeaderSeparatorBorder extends AbstractBorder {

		protected Color color;

		public HeaderSeparatorBorder(Color color) {
			super();

			this.color = color;
		}

		/**
		 * Paints the border for the specified component with the specified
		 * position and size.
		 * 
		 * @param c
		 *            the component for which this border is being painted
		 * @param g
		 *            the paint graphics
		 * @param x
		 *            the x position of the painted border
		 * @param y
		 *            the y position of the painted border
		 * @param width
		 *            the width of the painted border
		 * @param height
		 *            the height of the painted border
		 */
		public void paintBorder(Component c, Graphics g, int x, int y,
				int width, int height) {
			Color oldColor = g.getColor();
			g.setColor(color);
			g.drawLine(x, y + height - 1, x + width - 1, y + height - 1);

			g.setColor(oldColor);
		}

		/**
		 * Returns the insets of the border.
		 * 
		 * @param c
		 *            the component for which this border insets value applies
		 */
		public Insets getBorderInsets(Component c) {
			return new Insets(0, 0, 1, 0);
		}

		/**
		 * Reinitialize the insets parameter with this Border's current Insets.
		 * 
		 * @param c
		 *            the component for which this border insets value applies
		 * @param insets
		 *            the object to be reinitialized
		 */
		public Insets getBorderInsets(Component c, Insets insets) {
			insets.left = insets.top = insets.right = insets.bottom = 1;
			return insets;
		}

	}
}
