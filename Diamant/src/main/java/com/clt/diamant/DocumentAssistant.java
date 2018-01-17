/*
 * @(#)DocumentAssistant.java
 * Created on Mon Aug 08 2005
 *
 * Copyright (c) 2004 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */
package com.clt.diamant;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.clt.dialog.client.RendezvousCLTConnector;
import com.clt.dialog.client.ServerDevice;
import com.clt.dialog.client.ServiceInfoDescription;
import com.clt.gui.Assistant;
import com.clt.gui.AssistantPanel;
import com.clt.gui.CmdButton;
import com.clt.gui.border.GroupBorder;

/**
 *
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class DocumentAssistant extends Assistant {
    private JList devices;

    public DocumentAssistant(final SingleDocument doc) {

        super(Resources.getString("NewDialog"));

        AssistantPanel dienste = new AssistantPanel("devices", new BorderLayout()) {

            @Override
            public String getNextPanel() {

                return null;
            }

            @Override
            public boolean isComplete() {

                return true;
            }
        };

        this.devices = new JList(new Vector<Device>(doc.getDevices()));

        try {
            JPanel availableClients = new JPanel(new BorderLayout());
            final JList deviceList = new JList(ServerDevice.getAllClients());
            availableClients.add(new JScrollPane(deviceList));
            availableClients.setBorder(new GroupBorder(Resources.getString("AvailableDevices")));
            dienste.add(availableClients, BorderLayout.NORTH);

            JPanel docClients = new JPanel(new BorderLayout());
            docClients.add(new JScrollPane(this.devices));
            docClients.setBorder(new GroupBorder(Resources.getString("Devices")));
            dienste.add(docClients, BorderLayout.SOUTH);

            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
            final JButton add = new CmdButton(Resources.getString("Add"), new Runnable() {
                        public void run() {

                            ServiceInfoDescription si = (ServiceInfoDescription) deviceList.getSelectedValue();
                            if (si != null) {
                                Device d = new Device(si.getServiceName());
                                d.setConnector(new RendezvousCLTConnector(si.getServiceName(), si.getHostname()));
                                doc.getDevices().add(d);
                                DocumentAssistant.this.devices.setListData(new Vector<Device>(doc.getDevices()));
                            }
                        }
                    });
            
            final JButton remove = new CmdButton(Resources.getString("Remove"), new Runnable() {
                        public void run() {
                            Object[] devs = DocumentAssistant.this.devices.getSelectedValues();
                            if (devs != null) {
                                for (int i = 0; i < devs.length; i++) {
                                    doc.getDevices().remove(devs[i]);
                                }
                            }
                            DocumentAssistant.this.devices.setListData(new Vector<Device>(doc.getDevices()));
                        }
                    });

            final Runnable updateButtons = new Runnable() {

                public void run() {

                    remove.setEnabled((DocumentAssistant.this.devices
                            .getSelectedIndices().length > 0)
                            && DocumentAssistant.this.devices.hasFocus());
                    add.setEnabled((deviceList.getSelectedIndices().length > 0)
                            && deviceList.hasFocus());
                }
            };

            ListSelectionListener lsl = new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent evt) {

                    updateButtons.run();
                }
            };
            FocusListener fl = new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent evt) {

                    updateButtons.run();
                }
            };
            this.devices.addListSelectionListener(lsl);
            this.devices.addFocusListener(fl);
            deviceList.addListSelectionListener(lsl);
            deviceList.addFocusListener(fl);

            buttons.add(add);
            buttons.add(remove);
            dienste.add(buttons, BorderLayout.CENTER);
        } catch (Exception exn) {
            dienste.add(new JLabel(exn.toString()));
        }
        this.add(dienste);
    }

    public static SingleDocument createDocument() {

        SingleDocument d = new SingleDocument();
        DocumentAssistant assistant = new DocumentAssistant(d);

        if (assistant.show(null, "devices")) {
            return d;
        } else {
            return null;
        }
    }
}
