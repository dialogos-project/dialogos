package com.clt.diamant.graph.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import com.clt.diamant.graph.Node;

/**
 * @author dabo
 *
 */
public class NodeTransferable implements Transferable {

    // The data type exported from JColorChooser.
    private static String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=" + Node.class.getName();
    public static DataFlavor flavor = null;

    private Node node;

    public NodeTransferable(Node node) {

        try {
            if (NodeTransferable.flavor == null) {
                NodeTransferable.flavor = new DataFlavor(NodeTransferable.mimeType);
            }
        } catch (ClassNotFoundException exn) {
            // ignore
        }

        this.node = node;
    }

    public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException {

        if (flavor == NodeTransferable.flavor) {
            return this.node;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

    public DataFlavor[] getTransferDataFlavors() {

        return new DataFlavor[]{NodeTransferable.flavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {

        return flavor == NodeTransferable.flavor;
    }

}
