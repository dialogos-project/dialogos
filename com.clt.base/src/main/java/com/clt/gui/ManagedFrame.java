package com.clt.gui;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

public class ManagedFrame extends JFrame {

    private static Map<ManagedFrame, Collection<ManagedFrame>> gMainWindows = new WeakHashMap<ManagedFrame, Collection<ManagedFrame>>();

    private static Map<ManagedFrame, ManagedFrame> gWindows = new WeakHashMap<ManagedFrame, ManagedFrame>();

    private static ManagedFrame activeWindow = null;

    private static WindowListener windowUpdate = new WindowAdapter() {

        @Override
        public void windowActivated(WindowEvent evt) {
            if (evt.getWindow() instanceof ManagedFrame) {
                ManagedFrame.activeWindow = (ManagedFrame) evt.getWindow();
                ManagedFrame.activeWindow.rebuildWindowMenu();
            } else {
                System.err.println("Not a managed frame");
            }
        }

        @Override
        public void windowDeactivated(WindowEvent evt) {
            if (ManagedFrame.activeWindow == evt.getWindow()) {
                ManagedFrame.activeWindow = null;
            }
        }
    };

    public ManagedFrame() {
        super();
    }

    public ManagedFrame(String title) {
        super(title);
    }

    @Override
    public void addNotify() {
        super.addNotify();

        this.addWindowListener(ManagedFrame.windowUpdate);

        // System.out.println("addNotify() for " + this);
        synchronized (ManagedFrame.gMainWindows) {
            if (ManagedFrame.gWindows.get(this) == null) {
                ManagedFrame mainWindow = this.getMainWindow();
                if (mainWindow == null) {
                    mainWindow = this;
                }
                Collection<ManagedFrame> children = ManagedFrame.gMainWindows.get(mainWindow);
                if (children == null) {
                    children = new LinkedList<ManagedFrame>();
                    ManagedFrame.gMainWindows.put(mainWindow, children);
                }
                ManagedFrame.gWindows.put(this, mainWindow);
                children.add(ManagedFrame.this);
            }
        }
    }

    @Override
    public void removeNotify() {
        // System.out.println("removeNotify() for " + this);
        synchronized (ManagedFrame.gMainWindows) {
            ManagedFrame mainWindow = ManagedFrame.gWindows.get(this);

            // if mainWindow is null, we are already disposed
            if (mainWindow != null) {
                ManagedFrame.gWindows.remove(this);
                Collection<ManagedFrame> children = ManagedFrame.gMainWindows.get(mainWindow);

                // System.out.println("removed frame = " + this);
                // System.out.println("main window = " + mainWindow);
                children.remove(ManagedFrame.this);
                if (children.size() == 0) {
                    // System.out.println("Removed last child of " + mainWindow);
                    ManagedFrame.gMainWindows.remove(mainWindow);
                }
            }
        }

        this.removeWindowListener(ManagedFrame.windowUpdate);

        super.removeNotify();
    }

    @Override
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public final boolean equals(Object o) {
        return this == o;
    }

    protected void rebuildWindowMenu() {
        JMenu m = this.getWindowMenu();
        if (m != null) {
            m.removeAll();

            /*
       * JMenuItem item = new JMenuItem("Zoom window");
       * item.addActionListener(new ActionListener() { public void
       * actionPerformed(ActionEvent evt) { Document doc = (Document)
       * e.getSource(); doc.setLocation(0, 0);
       * doc.setSize(doc.getToolkit().getScreenSize()); } }); m.add(item);
             */
            JMenuItem item = new JMenuItem(GUI.getString("MinimizeWindow"));
            item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M,
                    item.getToolkit().getMenuShortcutKeyMask()));
            item.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent evt) {

                    ManagedFrame.activeWindow.setState(Frame.ICONIFIED);
                }
            });
            m.add(item);

            m.addSeparator();

            ManagedFrame.fillWindowMenu(m);
        }
    }

    // override to group windows
    protected ManagedFrame getMainWindow() {
        return this;
    }

    // override to have an automatic window menu
    protected JMenu getWindowMenu() {
        return null;
    }

    public static void fillWindowMenu(JMenu m) {
        synchronized (ManagedFrame.gMainWindows) {
            Set<ManagedFrame> sortedFrames = new TreeSet<ManagedFrame>(
                    new Comparator<ManagedFrame>() {

                public int compare(ManagedFrame o1, ManagedFrame o2) {

                    int result = o1.getTitle().compareTo(o2.getTitle());
                    if (result != 0) {
                        return result;
                    } else {
                        return System.identityHashCode(o2)
                                - System.identityHashCode(o1);
                    }
                }
            });
            sortedFrames.addAll(ManagedFrame.gMainWindows.keySet());

            for (final ManagedFrame mainWindow : sortedFrames) {
                Collection<ManagedFrame> children
                        = ManagedFrame.gMainWindows.get(mainWindow);

                if (children == null) {
                    continue;
                }

                if (children.size() == 1) {
                    final JRadioButtonMenuItem mi
                            = new JRadioButtonMenuItem(mainWindow.getTitle());
                    m.add(mi);
                    mi.setSelected(mainWindow == ManagedFrame.activeWindow);
                    mi.addActionListener(new ActionListener() {

                        public void actionPerformed(ActionEvent evt) {

                            if (mainWindow != ManagedFrame.activeWindow) {
                                mainWindow.toFront();
                            } else {
                                mi.setSelected(true);
                            }
                        }
                    });
                } else {
                    JMenu submenu = new JMenu(mainWindow.getTitle());
                    m.add(submenu);
                    for (final ManagedFrame child : children) {
                        final JRadioButtonMenuItem mi
                                = new JRadioButtonMenuItem(child.getTitle());
                        submenu.add(mi);
                        mi.setSelected(child == ManagedFrame.activeWindow);
                        mi.addActionListener(new ActionListener() {

                            public void actionPerformed(ActionEvent evt) {

                                if (child != ManagedFrame.activeWindow) {
                                    child.toFront();
                                } else {
                                    mi.setSelected(true);
                                }
                            }
                        });
                    }
                }
            }
        }
    }
}
