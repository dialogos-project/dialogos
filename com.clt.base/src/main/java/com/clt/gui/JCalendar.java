package com.clt.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */
public class JCalendar extends JPanel {

    private Calendar cal;

    private JLabel title;

    private CalPanel table;

    private boolean showWeekInYear = true;

    @SuppressWarnings("unused")
    private Date selectedWeek = null;

    public JCalendar(int month, int year) {

        this.cal = Calendar.getInstance();
        this.cal.setFirstDayOfWeek(Calendar.MONDAY);
        this.cal.set(Calendar.DAY_OF_MONTH, 1);
        this.cal.set(Calendar.MONTH, month);
        this.cal.set(Calendar.YEAR, year);

        this.setLayout(new BorderLayout());
        this.setFont(GUI.getSmallSystemFont());
        this.setBackground(Color.white);

        this.title = new JLabel("", SwingConstants.CENTER);
        this.title.setFont(this.getFont().deriveFont(Font.BOLD,
                this.getFont().getSize() + 0.5f));
        this.add(this.title, BorderLayout.NORTH);

        this.table = new CalPanel(this.getShowWeekInYear());
        this.add(this.table, BorderLayout.CENTER);

        this.localizeComponents();
    }

    private void localizeComponents() {

        this.title.setText(new SimpleDateFormat("MMMMM yyyy").format(this.cal
                .getTime()));
        this.table.localizeComponents();
    }

    @Override
    public void updateUI() {

        super.updateUI();

        if (this.cal != null) {
            this.localizeComponents();
        }
    }

    public boolean getShowWeekInYear() {

        return this.showWeekInYear;
    }

    public void setShowWeekInYear(boolean showWeekInYear) {

        this.showWeekInYear = showWeekInYear;
        this.table.setShowWeekInYear(showWeekInYear);
    }

    public void setSelectedWeek(Date d) {

        // TODO Implement week selection
    }

    private int dayDiff(Calendar c1, Calendar c2) {

        Calendar d1 = (Calendar) c1.clone();
        Calendar d2 = (Calendar) c2.clone();

        d1.set(Calendar.HOUR, 12);
        d1.set(Calendar.MINUTE, 0);
        d1.set(Calendar.SECOND, 0);
        d2.set(Calendar.HOUR, 12);
        d2.set(Calendar.MINUTE, 0);
        d2.set(Calendar.SECOND, 0);

        double ms_per_day = 24 * 60 * 60 * 1000;
        return (int) Math.round((d2.getTime().getTime() - d1.getTime().getTime())
                / ms_per_day);

        /*
     * int y1 = c1.get(Calendar.YEAR); int y2 = c2.get(Calendar.YEAR); int m1 =
     * c1.get(Calendar.MONTH); int m2 = c2.get(Calendar.MONTH); int d1 =
     * c1.get(Calendar.DAY_OF_MONTH); int d2 = c2.get(Calendar.DAY_OF_MONTH); if
     * (y1 == y2) { if (m1 == m2) return d2 - d1; else return m2 - m1; } else
     * return y2 - y1;
         */
    }

    private class CalPanel
            extends JPanel {

        JLabel[] days;

        List<Component> rowStart;

        List<Component> rowEnd;

        JComponent today = null;

        public CalPanel(boolean showWeekInYear) {

            this.initComponents(showWeekInYear);
        }

        public void setShowWeekInYear(boolean showWeekInYear) {

            this.initComponents(showWeekInYear);
        }

        private void initComponents(boolean showWeekInYear) {

            this.removeAll();

            this.days = new JLabel[7];
            this.rowStart = new Vector<Component>();
            this.rowEnd = new Vector<Component>();
            this.setOpaque(false);

            int columns = this.days.length + (showWeekInYear ? 1 : 0);

            this.setLayout(new GridLayout(0, columns));

            if (showWeekInYear) {
                this.add(new JLabel(""));
            }

            for (int i = 0; i < this.days.length; i++) {
                this.days[i] = new JLabel("", SwingConstants.CENTER);
                this.days[i].setFont(JCalendar.this.getFont().deriveFont(Font.BOLD));

                this.add(this.days[i]);
            }

            Calendar c = (Calendar) JCalendar.this.cal.clone();
            c.set(Calendar.DAY_OF_MONTH, 1);

            int offset = c.get(Calendar.DAY_OF_WEEK) - c.getFirstDayOfWeek();

            Font f = JCalendar.this.getFont().deriveFont(Font.PLAIN);

            Calendar now = Calendar.getInstance();

            for (int n = 0; (n < offset)
                    || (c.get(Calendar.MONTH) == JCalendar.this.cal.get(Calendar.MONTH))
                    || (n % columns != 0); n++) {
                JComponent child;
                boolean isKW = showWeekInYear && (n % columns == 0);
                if (isKW) {
                    child
                            = new JLabel("KW" + String.valueOf(c.get(Calendar.WEEK_OF_YEAR)),
                                    SwingConstants.RIGHT);
                } else {
                    if ((n >= offset)
                            && (c.get(Calendar.MONTH) == JCalendar.this.cal.get(Calendar.MONTH))) {
                        child
                                = new JLabel(String.valueOf(n - offset + 1), SwingConstants.RIGHT);
                    } else {
                        child = new JPanel();
                    }

                    if (JCalendar.this.dayDiff(c, now) == 0) {
                        this.today = child;
                        child.setForeground(SystemColor.textHighlightText);
                    }
                }

                child.setOpaque(false);
                child.setFont(f);
                child.setBorder(BorderFactory.createEmptyBorder(4, 3, 1, 3));
                this.add(child);
                if (n % columns == 0) {
                    this.rowStart.add(child);
                } else if (n % columns == columns - 1) {
                    this.rowEnd.add(child);
                }

                if ((n >= offset) && !isKW) {
                    c.add(Calendar.DAY_OF_MONTH, 1);
                }
            }

            this.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
        }

        @Override
        protected void paintComponent(Graphics g) {

            super.paintComponent(g);

            g.setColor(new Color(212, 212, 212));

            for (int i = 0; i < this.rowStart.size(); i++) {
                Rectangle start = ((JComponent) this.rowStart.get(i)).getBounds();
                Rectangle end = ((JComponent) this.rowEnd.get(i)).getBounds();

                @SuppressWarnings("unused")
                int width = end.x + end.width - start.x;
                int height = Math.max(start.height, end.height);
                // System.out.println(width + ", " + height);
                g.fillRoundRect(3, Math.min(start.y, end.y) + 2, this.getWidth() - 6,
                        height - 3,
                        height - 3, height - 3);
            }

            if (this.today != null) {
                g.setColor(SystemColor.textHighlight);
                g.fillRect(this.today.getX(), this.today.getY() + 2, this.today
                        .getWidth(), this.today.getHeight() - 2);
            }
        }

        void localizeComponents() {

            DateFormat format = new SimpleDateFormat("E");
            Calendar c = (Calendar) JCalendar.this.cal.clone();
            for (int i = 0; i < this.days.length; i++) {
                c.set(Calendar.DAY_OF_WEEK, JCalendar.this.cal.getFirstDayOfWeek() + i);
                this.days[i].setText(format.format(c.getTime()));
            }
        }

    }
}
