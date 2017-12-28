package com.clt.diamant.log;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.event.MouseInputAdapter;

import com.clt.diamant.graph.Node;
import com.clt.gui.GUI;
import com.clt.util.StringTools;

public class TimeLine
    extends JPanel {

  // Defaultwert fuer pixelsPerSecond. Sollte durch 20 teilbar sein, damit die
  // Skalierung
  // glatte Werte ergibt.
  private static final int INITIAL_PPS = 20;

  public static final int H_BORDER = 20, V_BORDER = 10, LINE_HEIGHT = 15;

  private int pixelsPerSecond;
  private int endTime;
  private int numLines;

  private List<String> lines;
  private List<EventUI> events;
  private Map<String, List<SwitchEventUI>> switches;
  private int currentX;


  public TimeLine(int duration, List<LogEvent<?>> logEvents,
      final ActionListener selectionListener) {

    this.currentX = -1;
    this.setAutoscrolls(true);
    this.endTime = duration;
    this.setFont(GUI.getSmallSystemFont());

    Map<String, List<Object>> types = new HashMap<String, List<Object>>();
    for (LogEvent<?> e : logEvents) {
      if (e instanceof InputLogEvent) {
        List<Object> v = types.get(e.getType());
        if (v == null) {
          v = new ArrayList<Object>();
        }
        if ((v.size() <= 2) && !v.contains(e.getArgument(0))) {
          // neuen Input gefunden. Dabei interessiert uns alles nach dem dritten
          // nicht.
          v.add(e.getArgument(0));
          types.put(e.getType(), v);
        }
      }
      else {
        types.put(e.getType(), Collections.emptyList());
      }
    }

    this.numLines = types.size();
    this.lines = new ArrayList<String>(types.keySet());

    this.events = new ArrayList<EventUI>();
    this.switches = new HashMap<String, List<SwitchEventUI>>();

    for (final LogEvent<?> logEvent : logEvents) {
      int line = this.lines.indexOf(logEvent.getType());
      EventUI eui;
      if (logEvent instanceof InputLogEvent) {
        List<Object> entries = types.get(logEvent.getType());
        if (entries.size() == 2) {
          Color c;
          if (logEvent.getArgument(0).equals("in")) {
            c = Color.green;
          }
          else {
            c = Color.red;
          }
          SwitchEventUI sw_eui = new SwitchEventUI(logEvent, c,
                        logEvent.getArgument(0).toString());
          eui = sw_eui;
          List<SwitchEventUI> v = this.switches.get(logEvent.getType());
          if (v == null) {
            v = new ArrayList<SwitchEventUI>();
          }
          v.add(sw_eui);
          this.switches.put(logEvent.getType(), v);
        }
        else {
          eui =
            new EventUI(logEvent, ((InputLogEvent)logEvent).logOnly()
              ? Color.orange
                            : Color.red);
          eui.setToolTipText(logEvent.getArgs());
        }
      }
      else if (logEvent.getType().equals("transition")) {
        eui = new EventUI(logEvent, Color.blue);
        Node src = (Node)logEvent.getArgument(0);
        Node dst = (Node)logEvent.getArgument(1);
        if (dst != null) {
          eui.setToolTipText(src.getTitle() + " -> " + dst.getTitle());
        }
        else {
          eui.setToolTipText(logEvent.getArgument(2) + " " + src.getTitle());
        }
      }
      else if (logEvent.getType().equals("prompt")) {
        eui = new EventUI(logEvent, Color.cyan);
        eui.setToolTipText("<html><b>"
                        + StringTools
                          .toHTML(logEvent.getArgument(0).toString())
          + ":</b><br>"
                        + StringTools
                          .toHTML(logEvent.getArgument(1).toString())
          + "</html>");
      }
      else {
        eui = new EventUI(logEvent, Color.red);
        eui.setToolTipText(logEvent.getArgs());
      }
      eui.setTime(logEvent.getTime());
      eui.setLine(line);
      this.events.add(eui);
    }

    GUI.addMouseInputListener(this, new MouseInputAdapter() {

      private EventUI findEventUI(MouseEvent evt) {

        int x = evt.getX();
        int y = evt.getY();

        for (EventUI ui : TimeLine.this.events) {
          if (ui.contains(x, y)) {
            return ui;
          }
        }

        return null;
      }


      @Override
      public void mousePressed(MouseEvent evt) {

        EventUI eventUI = this.findEventUI(evt);
        if (eventUI != null) {
          TimeLine.this.currentX = -1;
          selectionListener.actionPerformed(new ActionEvent(eventUI.getEvent(),
                        ActionEvent.ACTION_PERFORMED, "selected"));
        }
        else {
          TimeLine.this.currentX = evt.getX();
          this.mouseDragged(evt);
        }
      }


      @Override
      public void mouseDragged(MouseEvent evt) {

        if (TimeLine.this.currentX != -1) {
          TimeLine.this.currentX = evt.getX();
          long time =
            (TimeLine.this.currentX - TimeLine.H_BORDER) * 1000
              / TimeLine.this.pixelsPerSecond;
          if (time >= 0) {
            selectionListener
              .actionPerformed(new ActionEvent(TimeLine.this,
                            ActionEvent.ACTION_PERFORMED, String.valueOf(time)));
            TimeLine.this.repaint();
          }
        }
      }


      @Override
      public void mouseReleased(MouseEvent evt) {

        TimeLine.this.currentX = -1;
        TimeLine.this.repaint();
      }


      @Override
      public void mouseMoved(MouseEvent evt)
            {

              EventUI ui = this.findEventUI(evt);
              if (ui != null) {
                TimeLine.this.setToolTipText(ui.getToolTipText());
              }
              else {
                TimeLine.this.setToolTipText(null);
              }
            }
    });

    this.setPixelsPerSecond(TimeLine.INITIAL_PPS);
  }


  public int getPixelsPerSecond() {

    return this.pixelsPerSecond;
  }


  public void setPixelsPerSecond(int pixelsPerSecond) {

    this.pixelsPerSecond = pixelsPerSecond;
    this.setSize(this.getPreferredSize());
    this.recalcEventPositions();
  }


  private void recalcEventPositions() {

    for (EventUI eui : this.events) {
      if (eui instanceof SwitchEventUI) {
        eui.setLocation(TimeLine.H_BORDER
          + (int)(eui.getTime() * this.pixelsPerSecond / 1000),
          TimeLine.V_BORDER
                        + eui.getLine() * TimeLine.LINE_HEIGHT
            + TimeLine.LINE_HEIGHT / 2 - eui.getHeight() / 2);
      }
      else {
        eui.setLocation(TimeLine.H_BORDER
          + (int)(eui.getTime() * this.pixelsPerSecond / 1000)
                        - eui.getWidth() / 2, TimeLine.V_BORDER + eui.getLine()
          * TimeLine.LINE_HEIGHT + TimeLine.LINE_HEIGHT
                        / 2 - eui.getHeight() / 2);
      }
    }

    for (List<SwitchEventUI> v : this.switches.values()) {
      for (int i = 0; i < v.size() - 1; i++) {
        SwitchEventUI eui = v.get(i);
        SwitchEventUI next = v.get(i + 1);
        eui.setSize(next.getX() - eui.getX(), eui.getHeight());
      }
      SwitchEventUI eui = v.get(v.size() - 1);
      eui.setSize(this.getWidth() - TimeLine.H_BORDER - eui.getX(), eui
        .getHeight());
    }
  }


  public int translate(int oldX, int oldPPS) {

    long time = (oldX - TimeLine.H_BORDER) * 1000 / oldPPS;
    if (time <= 0) {
      return 0;
    }
    else {
      return (int)(time * this.pixelsPerSecond / 1000) + TimeLine.H_BORDER;
    }
  }


  @Override
  public void layout() {

    this.doLayout();
  }


  @Override
  public void doLayout() {

  }


  public JComponent createHeader() {

    JPanel p = new JPanel() {

      @Override
      public void paintComponent(Graphics g) {

        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        g.setColor(this.getForeground());
        g.setFont(TimeLine.this.getFont());
        FontMetrics fm = g.getFontMetrics(g.getFont());
        int base = fm.getDescent();

        for (int i = 0; i < TimeLine.this.numLines; i++) {
          g.drawString(TimeLine.this.lines.get(i), 3, TimeLine.V_BORDER
            + TimeLine.LINE_HEIGHT * (i + 1) - base);
        }

        // g.drawString("Seconds:", 3, V_BORDER + LINE_HEIGHT*numLines + 20);
      }


      @Override
      public Dimension getPreferredSize() {

        return new Dimension(70, TimeLine.this.getPreferredSize().height);
      }


      @Override
      public boolean isOpaque() {

        return true;
      }
    };
    p.setBackground(this.getBackground());
    // p.setBorder(BorderFactory.createEtchedBorder());

    return p;
  }


  public static String timeString(long time) {

    // return String.valueOf(time);
    return String.valueOf(time / 60) + ':' + String.valueOf((time % 60) / 10)
                + String.valueOf((time % 60) % 10);
  }


  @Override
  protected void paintComponent(Graphics g) {

    FontMetrics fm = g.getFontMetrics(g.getFont());

    Rectangle clip = g.getClipBounds();
    Rectangle bounds;
    if (this.getParent() instanceof JViewport) {
      bounds = ((JViewport)this.getParent()).getViewRect();
    }
    else {
      bounds = new Rectangle(0, 0, this.getWidth(), this.getHeight());
    }

    if (clip == null) {
      clip = bounds;
    }
    else {
      clip = clip.intersection(bounds);
    }

    g.setColor(this.getBackground());
    g.fillRect(clip.x, clip.y, clip.width, clip.height);

    if ((this.currentX != -1) && (this.currentX >= clip.x)
      && (this.currentX <= clip.x + clip.width)) {
      g.setColor(Color.red);
      // g.fillRect(currentX, clip.y, 1, clip.height);
      g.drawLine(this.currentX, clip.y, this.currentX, clip.y + clip.height);
      g.drawLine(this.currentX - 1, clip.y, this.currentX - 1, clip.y
        + clip.height);
    }

    g.setColor(this.getForeground());

    for (int i = 0; i <= this.numLines; i++) {
      int y = TimeLine.V_BORDER + i * TimeLine.LINE_HEIGHT;
      if ((y >= clip.y) && (y <= clip.y + clip.height)) {
        g.drawLine(Math.max(TimeLine.H_BORDER, clip.x), y, Math.min(
          TimeLine.H_BORDER + (this.endTime / 1000 + 1)
                      * this.pixelsPerSecond, clip.x + clip.width), y);
      }
    }

    for (int i = 0; i <= this.endTime / 1000 + 1; i++) {
      int x = i * this.pixelsPerSecond + TimeLine.H_BORDER;

      // make sure we don't draw the legend too often
      int maxWidth =
        fm.stringWidth(TimeLine.timeString(this.endTime / 1000 + 1)) + 10;
      int factor = maxWidth / this.pixelsPerSecond + 1;

      if ((x >= clip.x) && (x <= clip.x + clip.width)) {
        g.drawLine(x, TimeLine.V_BORDER, x, TimeLine.V_BORDER + this.numLines
          * TimeLine.LINE_HEIGHT
                      + (i % factor == 0 ? 5 : 2));
      }

      String s = TimeLine.timeString(i);
      int width = fm.stringWidth(s);
      if ((x + width / 2 >= clip.x) && (x - width / 2 <= clip.x + clip.width)) {
        if (i % factor == 0) {
          g.drawString(s, x - width / 2, TimeLine.V_BORDER + this.numLines
            * TimeLine.LINE_HEIGHT + 20);
        }
      }
    }
  }


  @Override
  protected void paintChildren(Graphics g) {

    Rectangle clip = g.getClipBounds();
    if (clip == null) {
      clip = this.getBounds();
    }
    for (EventUI ui : this.events) {
      ui.paint(g);
    }

    super.paintChildren(g);
  }


  @Override
  public Dimension getPreferredSize() {

    return new Dimension((this.endTime / 1000 + 1) * this.pixelsPerSecond + 2
      * TimeLine.H_BORDER, this.numLines
                * TimeLine.LINE_HEIGHT + 20 + 2 * TimeLine.V_BORDER);
  }

  private static class EventUI {

    protected Color bodyColor;
    protected Color frameColor;
    private int line;
    private long time;
    private String tooltip;
    private int x;
    private int y;
    private int width;
    private int height;
    private LogEvent<?> event;


    public EventUI(LogEvent<?> event, Color bodyColor) {

      this(event, bodyColor, null);
      this.setSize(5, 5);
    }


    public EventUI(LogEvent<?> event, Color bodyColor, Color frameColor) {

      this.event = event;
      this.bodyColor = bodyColor;
      this.frameColor = frameColor;
    }


    public LogEvent<?> getEvent() {

      return this.event;
    }


    public int getX() {

      return this.x;
    }


    public int getY() {

      return this.y;
    }


    public void setLocation(int x, int y) {

      this.x = x;
      this.y = y;
    }


    public int getWidth() {

      return this.width;
    }


    public int getHeight() {

      return this.height;
    }


    public void setSize(int width, int height) {

      this.width = width;
      this.height = height;
    }


    public Dimension getSize() {

      return new Dimension(this.getWidth(), this.getHeight());
    }


    public boolean contains(int x, int y) {

      return (x >= this.y) && (y >= this.y) && (x < this.x + this.width)
                    && (y < this.y + this.height);
    }


    public void paint(Graphics g) {

      g.setColor(this.bodyColor);
      g.fillOval(this.x, this.y, this.getWidth(), this.getHeight());
      if (this.frameColor != null) {
        g.setColor(this.frameColor);
        g.drawOval(this.x, this.y, this.getWidth(), this.getHeight());
      }
    }


    public void setTime(long time) {

      this.time = time;
    }


    public long getTime() {

      return this.time;
    }


    public void setLine(int line) {

      this.line = line;
    }


    public int getLine() {

      return this.line;
    }


    public String getToolTipText() {

      return this.tooltip;
    }


    public void setToolTipText(String tooltip) {

      this.tooltip = tooltip;
    }
  }

  private static class SwitchEventUI
        extends EventUI {

    public SwitchEventUI(LogEvent<?> event, Color c, String s) {

      super(event, c);
      this.setToolTipText(s);
    }


    @Override
    public void paint(Graphics g) {

      g.setColor(this.bodyColor);
      g.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
  }
}