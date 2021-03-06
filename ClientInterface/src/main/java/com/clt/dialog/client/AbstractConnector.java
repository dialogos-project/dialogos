package com.clt.dialog.client;

import com.clt.properties.Property;

/**
 * @author dabo
 *
 */
abstract class AbstractConnector implements Connector {
    @Override
    public String toString() {
        return this.getName();
    }

    public String getDescription() {
        StringBuilder b = new StringBuilder();
        b.append(this.getName());
        Property<?>[] ps = this.getProperties();
        if (ps.length > 0) {
            b.append(" (");
            boolean first = true;
            for (int i = 0; i < ps.length; i++) {
                String p = ps[i].getValueAsString();
                if (p != null) {
                    if (first) {
                        first = false;
                    } else {
                        b.append(", ");
                    }
                    b.append(ps[i].getName());
                    b.append("=");
                    b.append(p == null ? " " : p);
                }
            }
            b.append(")");
        }
        return b.toString();
    }
}
