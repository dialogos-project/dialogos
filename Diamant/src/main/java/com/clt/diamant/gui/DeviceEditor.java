package com.clt.diamant.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import com.clt.dialog.client.Connector;
import com.clt.diamant.Device;
import com.clt.diamant.Resources;
import com.clt.diamant.graph.Node;
import com.clt.diamant.graph.ui.NodeComponent;
import com.clt.gui.Buttons;
import com.clt.gui.ImageChooser;
import com.clt.gui.Images;
import com.clt.gui.OptionPane;
import com.clt.gui.RawIcon;
import com.clt.properties.Property;

/**
 * @author dabo
 *
 */
public class DeviceEditor {

    private static Collection<Image> sDeviceIcons = null;

    public static boolean editDevice(Device device, Component parent) {

        Connector currentConnector = device.getConnector();

        Vector<Connector> connectors = new Vector<Connector>();
        Connector cn;
        if ((cn
                = DeviceEditor.addConnector(connectors,
                        new com.clt.dialog.client.ManualCLTConnector(),
                        currentConnector)) != null) {
            currentConnector = cn;
        }
        if ((cn
                = DeviceEditor.addConnector(connectors,
                        new com.clt.dialog.client.RendezvousCLTConnector(),
                        currentConnector)) != null) {
            currentConnector = cn;
        }
        if ((cn
                = DeviceEditor.addConnector(connectors,
                        new com.clt.dialog.client.InternalConnector(),
                        currentConnector)) != null) {
            currentConnector = cn;
        }

        final JComboBox cb = new JComboBox(connectors);

        final JPanel p = new JPanel(new BorderLayout());
        final JPanel props_panel = new JPanel(new CardLayout());

        final JPanel sel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(3, 3, 3, 3);

        gbc.weightx = 0.0;
        sel.add(new JLabel(Resources.getString("Name") + ":"), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        gbc.gridwidth = 2;
        final JTextField nameField = new JTextField();
        nameField.setText(device.getName());
        nameField.selectAll();
        sel.add(nameField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;

        gbc.weightx = 0.0;
        sel.add(new JLabel(Resources.getString("Icon") + ":"), gbc);
        gbc.gridx++;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridwidth = 2;
        Device.Icon iconData = device.getIconData();

        final RawIcon icon;
        if (iconData == null) {
            icon = new RawIcon(null);
        } else {
            icon
                    = new RawIcon(iconData.getData(), iconData.getWidth(), iconData
                            .getHeight(), 0,
                            iconData.getWidth());
        }
        JLabel iconField = new JLabel(icon);
        iconField.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createEtchedBorder(),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        sel.add(iconField, gbc);
        gbc.gridy++;
        gbc.gridx = 0;

        gbc.weightx = 0.0;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        sel.add(new JLabel(Resources.getString("Connector") + ":"), gbc);
        gbc.gridx++;
        gbc.weightx = 1.0;
        sel.add(cb, gbc);
        gbc.gridx++;
        gbc.weightx = 0.0;

        final JButton helpButton = Buttons.createHelpButton();
        helpButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                Connector c = (Connector) cb.getSelectedItem();
                if (c == null) {
                    OptionPane.message(helpButton, "You must choose a connector.");
                } else {
                    OptionPane.message(helpButton, c.getHelp());
                }
            }
        });
        sel.add(helpButton, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 3;
        gbc.weightx = 1.0;
        sel.add(new JSeparator(), gbc);

        p.add(sel, BorderLayout.NORTH);
        p.add(props_panel, BorderLayout.CENTER);

        for (int i = 0; i < connectors.size(); i++) {
            JPanel props = new JPanel(new GridBagLayout());

            final Connector connector = connectors.get(i);
            Property<?>[] properties = connector.getProperties();

            GridBagConstraints gbc2 = new GridBagConstraints();
            gbc2.gridx = gbc2.gridy = 0;
            gbc2.insets = new Insets(4, 3, 4, 3);
            gbc2.ipadx = 4;
            gbc2.fill = GridBagConstraints.HORIZONTAL;

            for (int j = 0; j < properties.length; j++) {
                final String property = properties[j].getName();
                gbc2.weightx = 0.0;
                props.add(new JLabel(property), gbc2);
                gbc2.gridx++;
                gbc2.weightx = 1.0;
                props.add(properties[j].createEditor(false), gbc2);
                gbc2.gridx = 0;
                gbc2.gridy++;
            }

            gbc2.fill = GridBagConstraints.BOTH;
            gbc2.weighty = 1.0;
            props.add(Box.createVerticalGlue(), gbc2);

            props_panel.add(props, connector.toString());
        }

        props_panel.add(new JPanel(), "");

        iconField.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent evt) {

                Image im
                        = ImageChooser.selectImage(sel, DeviceEditor.getDeviceIcons(),
                                new Dimension(16, 16));
                if (im != null) {
                    icon.setImage(im);
                }
            }
        });

        cb.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent evt) {

                if (cb.getSelectedItem() != null) {
                    ((CardLayout) props_panel.getLayout()).show(props_panel,
                            cb.getSelectedItem().toString());
                } else {
                    ((CardLayout) props_panel.getLayout()).show(props_panel, "");
                }
            }
        });

        cb.setSelectedItem(currentConnector);

        int result
                = OptionPane.confirm(parent, p,
                        Resources.getString("EditDeviceProperties"),
                        OptionPane.OK_CANCEL_OPTION, OptionPane.PLAIN);
        if (result == OptionPane.OK) {
            device.setName(nameField.getText());
            device.setIconData(new Device.Icon(icon.getIconData(), icon
                    .getIconWidth()));
            device.setConnector((Connector) cb.getSelectedItem());
            return true;
        } else {
            return false;
        }
    }

    private static Connector addConnector(Collection<Connector> connectors,
            Connector connector,
            Connector currentConnector) {

        connectors.add(connector);
        if ((currentConnector != null)
                && connector.getClass().equals(currentConnector.getClass())) {
            Property<?>[] oldProperties = currentConnector.getProperties();
            Property<?>[] newProperties = connector.getProperties();

            for (int i = 0; i < newProperties.length; i++) {
                for (int j = 0; j < oldProperties.length; j++) {
                    if (newProperties[i].getID().equals(oldProperties[j].getID())) {
                        try {
                            newProperties[i].setValueFromString(oldProperties[j]
                                    .getValueAsString());
                        } catch (ParseException e) {
                        }
                    }
                }
            }
            return connector;
        } else {
            return null;
        }
    }

    private static Collection<Image> getDeviceIcons() {

        if (DeviceEditor.sDeviceIcons == null) {
            DeviceEditor.sDeviceIcons = new LinkedHashSet<Image>();

            Map<Object, List<Class<Node>>> allNodeTypes
                    = Node.getAvailableNodeTypes();
            for (List<Class<Node>> nodeTypes : allNodeTypes.values()) {
                for (Class<Node> nodeType : nodeTypes) {
                    Image image = Images.getImage(NodeComponent.getNodeIcon(nodeType));
                    if (image != null) {
                        DeviceEditor.sDeviceIcons.add(image);
                    }
                }
            }
        }
        return DeviceEditor.sDeviceIcons;
    }
}
