package com.clt.properties;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.clt.util.UniqueList;

/**
 * The PropertySet contains properties. The methods {@code read()} and
 * {@code write()} reads and writes this properties from or to an output-stream.
 * The {@code createPropertyPanel()} is a convenient way to create a property
 * panel displaying all those properties.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class PropertySet<T extends Property<?>> extends UniqueList<T> {

    public PropertySet() {

        super();
    }

    /**
     * The PropertySet object is initialized with the array of properties passed
     * as parameter.
     *
     * @param properties Array of properties
     */
    public PropertySet(T... properties) {

        this(properties != null ? Arrays.asList(properties) : null);
    }

    /**
     * The PropertySet object is initialized with the collection of properties
     * passed as parameter.
     *
     * @param properties Collection of properties
     */
    public PropertySet(Collection<? extends T> properties) {

        this();

        if (properties != null) {
            this.addAll(properties);
        }
    }

    /**
     * Writes out the properties id and value in the outputstream as
     * property-table.
     *
     * @param out output stream where the property table will be stored.
     */
    public void write(OutputStream out)
            throws IOException {

        Properties ps = new Properties();
        for (Iterator<T> it = this.iterator(); it.hasNext();) {
            T p = it.next();
            ps.setProperty(p.getID(), p.getValueAsString());
        }
        ps.store(out, null);
    }

    public void read(InputStream in)
            throws IOException, java.text.ParseException {

        Properties ps = new Properties();
        ps.load(in);
        for (Iterator<T> it = this.iterator(); it.hasNext();) {
            T property = it.next();
            String value = ps.getProperty(property.getID());
            if (value != null) {
                property.setValueFromString(value);
            }
        }
    }

    /**
     * Creates a property panel from all the properties contained in this
     * PropertySet. If fillHorizontally is true, the JComponents contained in
     * the returned panel will automatically fill all the horizontal space.
     *
     * @param fillHorizontally If true, the components will fill the horizontal
     * space.
     * @return a Reference on the created JPanel.
     */
    public JPanel createPropertyPanel(boolean fillHorizontally) {
        JPanel p = new JPanel(new GridBagLayout());

        boolean onlyCheckboxes = true;
        for (Property<?> property : this) {
            if (property.getEditType() != Property.EDIT_TYPE_CHECKBOX) {
                onlyCheckboxes = false;
            }
        }

        this.fillPropertyPanelImpl(p, null, fillHorizontally, onlyCheckboxes);

        return p;
    }

    /**
     * Adds the properties in this PropertySet to an existing JPanel.
     * The gridbag constraints are automatically updated, so subsequent
     * calls to this method simply put the next properties below the
     * previous ones. 
     * 
     * @param p
     * @param gbc
     * @param fillHorizontally If true, the components will fill the horizontal
     * space.
     */
    public void fillPropertyPanel(JPanel p, GridBagConstraints gbc, boolean fillHorizontally) {
        this.fillPropertyPanelImpl(p, gbc, fillHorizontally, false);
    }

    private void fillPropertyPanelImpl(JPanel p, GridBagConstraints gbc,
            boolean fillHorizontally,
            boolean onlyCheckboxes) {

        if (gbc == null) {
            gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(2, 3, 2, 3);
        }

        for (Iterator<T> it = this.iterator(); it.hasNext();) {
            T property = it.next();

            if (onlyCheckboxes) {
                JComponent c = property.createEditor(true);
                gbc.anchor = GridBagConstraints.WEST;
                p.add(c, gbc);
                gbc.gridy++;
            } else {
                JComponent c = property.createEditor(false);
                JLabel l = new JLabel(property.getName());
                
                addLabelAndEditorComponent(p, l, c, gbc, fillHorizontally);
            }
        }
    }

    /**
     * Adds an editor component and its label to a JPanel.
     * This is done in exactly the same way as {@link #fillPropertyPanel(javax.swing.JPanel, java.awt.GridBagConstraints, boolean) }
     * does it, so custom Swing components can be laid out alongside
     * the editor components of DialogOS properties.
     * 
     * @param p
     * @param label
     * @param editor
     * @param gbc
     * @param fillHorizontally 
     */
    public static void addLabelAndEditorComponent(JPanel p, JLabel label, JComponent editor, GridBagConstraints gbc, boolean fillHorizontally) {
        gbc.gridx = 0;
        if (fillHorizontally) {
            gbc.weightx = 0.0;
        } else {
            gbc.weightx = 1.0;
        }
        
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.NONE;
        label.setLabelFor(editor);
        label.setToolTipText(editor.getToolTipText());
        p.add(label, gbc);

        gbc.gridx++;
        gbc.weightx = 1.0;
        if (fillHorizontally) {
            gbc.fill = GridBagConstraints.HORIZONTAL;
        }
        gbc.anchor = GridBagConstraints.WEST;
        p.add(editor, gbc);
        
        gbc.gridy++;
    }

    public static PropertySet<Property<?>> createFromBean(Object object) throws IntrospectionException {
        return PropertySet.createFromBean(object, null);
    }

    public static PropertySet<Property<?>> createFromBean(final Object object,
            ResourceBundle resources)
            throws IntrospectionException {

        PropertySet<Property<?>> properties = new PropertySet<Property<?>>();

        BeanInfo info = Introspector.getBeanInfo(object.getClass());

        PropertyDescriptor[] p_desc = info.getPropertyDescriptors();
        if (p_desc != null) {
            for (int i = 0; i < p_desc.length; i++) {
                final PropertyDescriptor pd = p_desc[i];
                Class<?> type = pd.getPropertyType();
                final Method getter = pd.getReadMethod();
                final Method setter = pd.getWriteMethod();
                final String name
                        = PropertySet.loadResource(resources, pd.getDisplayName());
                final String description = pd.getShortDescription();
                Property<?> p = null;
                if ((type == Boolean.TYPE) || (type == Boolean.class)) {
                    p = new BooleanProperty(pd.getName()) {

                        @Override
                        public void setValueImpl(boolean value) {

                            try {
                                if (setter != null) {
                                    setter.invoke(object, new Object[]{Boolean.valueOf(value)});
                                }
                            } catch (Exception exn) {
                            }
                        }

                        @Override
                        public boolean getValue() {

                            try {
                                if (getter != null) {
                                    return ((Boolean) getter.invoke(object, new Object[0]))
                                            .booleanValue();
                                }
                            } catch (Exception exn) {
                            }
                            return false;
                        }

                        @Override
                        public String getName() {

                            return name;
                        }

                        @Override
                        public String getDescription() {

                            return description;
                        }
                    };
                } else if ((type == Integer.TYPE) || (type == Integer.class)) {
                    p = new IntegerProperty(pd.getName()) {

                        @Override
                        protected void setValueImpl(int value) {

                            try {
                                if (setter != null) {
                                    setter.invoke(object, new Object[]{Integer.valueOf(value)});
                                }
                            } catch (Exception exn) {
                            }
                        }

                        @Override
                        public int getValue() {

                            try {
                                if (getter != null) {
                                    return ((Integer) getter.invoke(object, new Object[0]))
                                            .intValue();
                                }
                            } catch (Exception exn) {
                            }
                            return 0;
                        }

                        @Override
                        public String getName() {

                            return name;
                        }

                        @Override
                        public String getDescription() {

                            return description;
                        }
                    };
                } else if ((type == Float.TYPE) || (type == Float.class)) {
                    p = new FloatProperty(pd.getName()) {

                        @Override
                        public void setValueImpl(float value) {

                            try {
                                if (setter != null) {
                                    setter.invoke(object, new Object[]{Float.valueOf(value)});
                                }
                            } catch (Exception exn) {
                            }
                        }

                        @Override
                        public float getValue() {

                            try {
                                if (getter != null) {
                                    return ((Float) getter.invoke(object, new Object[0]))
                                            .floatValue();
                                }
                            } catch (Exception exn) {
                            }
                            return Float.NaN;
                        }

                        @Override
                        public String getName() {

                            return name;
                        }

                        @Override
                        public String getDescription() {

                            return description;
                        }
                    };
                } else if (type == String.class) {
                    p = new StringProperty(pd.getName()) {

                        @Override
                        protected void setValueImpl(String value) {

                            try {
                                if (setter != null) {
                                    setter.invoke(object, new Object[]{value});
                                }
                            } catch (Exception exn) {
                            }
                        }

                        @Override
                        public String getValue() {

                            try {
                                if (getter != null) {
                                    return (String) getter.invoke(object, new Object[0]);
                                }
                            } catch (Exception exn) {
                            }
                            return null;
                        }

                        @Override
                        public String getName() {

                            return name;
                        }

                        @Override
                        public String getDescription() {

                            return description;
                        }
                    };
                } else if (type == Color.class) {
                    p = new ColorProperty(pd.getName()) {

                        @Override
                        public void setValueImpl(Color value) {

                            try {
                                if (setter != null) {
                                    setter.invoke(object, new Object[]{value});
                                }
                            } catch (Exception exn) {
                            }
                        }

                        @Override
                        public Color getValue() {

                            try {
                                if (getter != null) {
                                    return (Color) getter.invoke(object, new Object[0]);
                                }
                            } catch (Exception exn) {
                            }
                            return null;
                        }

                        @Override
                        public String getName() {

                            return name;
                        }

                        @Override
                        public String getDescription() {

                            return description;
                        }
                    };
                } else if (type == File.class) {
                    p = new FileProperty(pd.getName()) {

                        @Override
                        protected void setValueImpl(File value) {

                            try {
                                if (setter != null) {
                                    setter.invoke(object, new Object[]{value});
                                }
                            } catch (Exception exn) {
                            }
                        }

                        @Override
                        public File getValue() {

                            try {
                                if (getter != null) {
                                    return (File) getter.invoke(object, new Object[0]);
                                }
                            } catch (Exception exn) {
                            }
                            return null;
                        }

                        @Override
                        public String getName() {

                            return name;
                        }

                        @Override
                        public String getDescription() {

                            return description;
                        }
                    };
                }
                if (p != null) {
                    p.setEditable(setter != null);
                    properties.add(p);
                }
            }
        }

        return properties;
    }

    private static String loadResource(ResourceBundle resources, String key) {

        if ((resources == null) || (key == null)) {
            return key;
        }

        try {
            String o = resources.getString(key);
            return o != null ? o : key;
        } catch (Exception exn) {
            return key;
        }
    }
}
