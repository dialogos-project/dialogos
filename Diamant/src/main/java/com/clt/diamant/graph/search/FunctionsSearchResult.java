package com.clt.diamant.graph.search;

import javax.swing.JComponent;

import com.clt.diamant.SingleDocument;
import com.clt.diamant.graph.Functions;
import com.clt.diamant.graph.Graph;
import com.clt.diamant.graph.GraphOwner;
import com.clt.diamant.graph.ui.GraphUI;
import com.clt.diamant.gui.GraphEditorFactory;
import com.clt.diamant.gui.ScriptEditorDialog;

/**
 * @author dabo
 *
 */
public class FunctionsSearchResult extends SearchResult {

    private Graph owner;
    private Functions functions;

    public FunctionsSearchResult(Graph owner, Functions functions,
            String message, Type type) {

        super(null, message, type);

        this.owner = owner;
        this.functions = functions;
    }

    @Override
    public String getDocumentName() {

        if (this.getGraph() != null) {
            return this.getGraph().graphPath(false).toString() + ":"
                    + this.getSource();
        } else {
            return this.getSource();
        }
    }

    private Graph getGraph() {

        return this.owner;
    }

    @Override
    public String getSource() {

        return this.functions.getName();
    }

    @Override
    public boolean isRelevant() {

        if (this.getGraph() == null) {
            return true;
        } else {
            GraphOwner doc = this.getGraph().getMainOwner();
            return (doc instanceof SingleDocument ? GraphEditorFactory.isShowing(doc)
                    : false);
        }

    }

    @Override
    public GraphUI showResult(JComponent parent) {

        if (this.getGraph() == null) {
            this.getToolkit().beep();
            return null;
        } else {
            ScriptEditorDialog.editFunctions(parent, this.functions);
            /*
       * ScriptEditor editor = new ScriptEditor(ScriptEditor.Type.FUNCTIONS);
       * 
       * if (parent != null) { parent.removeAll(); parent.add();
       * parent.validate(); } else { ScriptEditorDialog.editFunctions(parent,
       * functions); }
             */
            return null;
        }
    }
}
