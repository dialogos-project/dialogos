package com.clt.dialog.client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import com.clt.event.DataEntryEvent;
import com.clt.event.DataEntryListener;
import com.clt.gui.DataEntry;
import com.clt.gui.OptionPane;
import com.clt.gui.ProgressDialog;
import com.clt.gui.RawIcon;
import com.clt.util.LongAction;

/**
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ConnectDialog implements ConnectionChooser {

  private Component parent;
  private JPanel panel;
  private Map<ServerDevice, JLabel> status_map;
  private Map<ServerDevice, ComboBox> chooser_map;


  public ConnectDialog(Component parent) {

    this.parent = parent;
  }


  public void init(final ServerDevice[] devices) {

    this.panel = new JPanel(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(3, 6, 3, 6);
    this.status_map = new Hashtable<ServerDevice, JLabel>();
    this.chooser_map = new Hashtable<ServerDevice, ComboBox>();

    gbc.gridy = 0;
    for (ServerDevice dev : devices) {
      JLabel status = new JLabel(null, null, SwingConstants.CENTER);
      status.setPreferredSize(new Dimension(40, 20));
      status.setOpaque(true);
      status.setBackground(Color.red);
      this.panel.add(status, gbc);
      gbc.gridy++;

      this.status_map.put(dev, status);
    }

    gbc.gridx++;
    gbc.gridy = 0;
    for (ServerDevice dev : devices) {
      ServerDevice.Icon iconData = dev.getIconData();
      Icon icon =
        iconData == null ? new RawIcon(null) : new RawIcon(iconData.getData(),
                iconData.getWidth());
      this.panel.add(
        new JLabel(dev.getName() + ":", icon, SwingConstants.LEFT), gbc);
      gbc.gridy++;
    }

    /*
     * gbc.gridx++; gbc.gridy = 0; for (ServerDevice dev : devices) { ComboBox
     * combobox = new ComboBox(new Object[0]); combobox.setPreferredSize(new
     * Dimension(140, 20)); combobox.setEnabled(false); panel.add(combobox,
     * gbc); gbc.gridy++;
     * 
     * chooser_map.put(dev, combobox); }
     */
  }


  public boolean resolve(final Map<ServerDevice, Object[]> ambiguities,
            final Map<ServerDevice, Object> defaults) {

    JPanel p = new JPanel(new GridBagLayout());

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = gbc.gridy = 0;
    gbc.insets = new Insets(3, 3, 3, 3);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    for (final ServerDevice d : ambiguities.keySet()) {
      final Object[] targets = ambiguities.get(d);

      gbc.gridx = 0;
      gbc.weightx = 0;
      p.add(new JLabel(d.getName()), gbc);
      gbc.gridx++;
      gbc.weightx = 1.0;
      final ComboBox cb = new ComboBox(targets);
      cb.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          if (cb.getSelectedIndex() >= 0) {
            ambiguities.put(d, new Object[] { targets[cb.getSelectedIndex()] });
          }
        }
      });
      cb.setSelectedItem(defaults.get(d));
      p.add(cb, gbc);
      gbc.gridy++;
    }

    boolean result =
      OptionPane
        .confirm(
            this.parent,
            new Object[] {
                    "For some devices multiple clients where found. Please select the clients you want to use.",
                    p }, "Select clients", OptionPane.OK_CANCEL_OPTION,
          OptionPane.PLAIN) == OptionPane.OK;

    return result;
  }


  public void start(LongAction action)
      throws InvocationTargetException {

    new ProgressDialog(this.parent, 0).run(action, this.panel);
  }


  public void stateChanged(ServerDevice d) {

    final JLabel c = this.status_map.get(d);
    ConnectDialog.showState(c, d.getState());

    if (d.getState() == ConnectionState.CONNECTED) {
      ComboBox chooser = this.chooser_map.get(d);
      if (chooser != null) {
        chooser.setModel(new DefaultComboBoxModel(
                    new Object[] { d.getConnector().getTarget() }));
        chooser.setEnabled(false);
      }
    }
  }


  public void protocolChanged(ServerDevice d, String protocol) {

    final JLabel c = this.status_map.get(d);
    c.setText(protocol);
  }


  public void ambiguityDetected(ServerDevice d,
      Map<ServerDevice, Object[]> ambiguities) {

    final JLabel c = this.status_map.get(d);
    ConnectDialog.showState(c, null);

    ComboBox chooser = this.chooser_map.get(d);
    if (chooser != null) {
      chooser.setModel(new DefaultComboBoxModel(ambiguities.get(d)));
      chooser.setEnabled(true);
    }
  }


  private static void showState(Component c, ConnectionState state) {

    if (c != null) {
      if (state == null) {
        c.setBackground(Color.YELLOW);
      }
      else {
        switch (state) {
          case CONNECTED:
            c.setBackground(Color.GREEN);
            break;

          case CONNECTING:
            c.setBackground(Color.RED);
            break;

          case DISCONNECTED:
            c.setBackground(Color.RED);
            break;

          default:
            c.setBackground(Color.YELLOW);
            break;
        }
      }
    }
  }

  private static class ComboBox
        extends JComboBox
        implements DataEntry {

    private Collection<DataEntryListener> dataEntryListeners =
      new ArrayList<DataEntryListener>();


    public ComboBox(Object values[]) {

      super(values);

      this.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent evt) {

          synchronized (ComboBox.this.dataEntryListeners) {
            DataEntryEvent e = new DataEntryEvent(ComboBox.this);
            for (DataEntryListener listener : ComboBox.this.dataEntryListeners) {
              listener.dataChanged(e);
            }
          }
        }
      });
    }


    public boolean dataEntered() {

      return this.getSelectedItem() != null;
    }


    public void addDataEntryListener(DataEntryListener listener) {

      synchronized (this.dataEntryListeners) {
        this.dataEntryListeners.add(listener);
      }
    }


    public void removeDataEntryListener(DataEntryListener listener) {

      synchronized (this.dataEntryListeners) {
        this.dataEntryListeners.remove(listener);
      }
    }
  }
}
