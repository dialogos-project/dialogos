package com.clt.diamant;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.clt.diamant.graph.search.SearchResult;
import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.OptionPane;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

import javax.swing.SwingUtilities;

public abstract class Document {

    public final static boolean validateXML = false;

    private static int gUntitledDocuments = 0;

    private boolean dirty = false;
    private boolean readOnly = false;
    private File file = null;
    private String title = null;
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public Document() {

        this(Resources.getString("Untitled") + ' ' + (++Document.gUntitledDocuments));
    }

    public Document(String title) {

        this.title = title;
    }

    public File getFile() {

        return this.file;
    }

    public void setFile(File file) {

        this.file = file;
        this.setTitle(file.getName());
    }

    public boolean isDirty() {

        return this.dirty;
    }

    public void setDirty(boolean dirty) {

        this.dirty = dirty;
    }

    public String getTitle() {

        return this.title;
    }

    public void setTitle(String title) {

        String oldTitle = this.title;
        this.title = title;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Document.this.firePropertyChange("title", oldTitle, title);
            }
        });
    }

    protected void writeHeader(XMLWriter out) {

        out.println("<!DOCTYPE " + this.getDocumentType().toLowerCase()
                + " SYSTEM \""
                + this.getDocumentType() + ".dtd\">");
        out.println();
    }

    /**
     * Saves this Document to a file, clears the dirty status of the Document,
     * and sets the window title to the new filename.
     *
     * @param parent
     * @param f
     * @param l
     * @throws Exception
     */
    public void save(Component parent, File f, ProgressListener l) throws Exception {
        save(parent, f, true, l);
    }

    /**
     * Saves this Document to a file. If isFullSave is set to true, this will
     * also clear the document's dirty status and set the window title to the
     * new filename. This is useful e.g. for the File -&gt; Save menu item and its
     * relatives. Alternatively, if you only want to save a copy of the Document
     * to a file without a noticeable change to the user, use this method with
     * isFullSave=false.
     *
     * @param parent
     * @param f
     * @param isFullSave
     * @param l
     * @throws Exception
     */
    public void save(Component parent, File f, boolean isFullSave, ProgressListener l) throws Exception {

        l.progressChanged(new ProgressEvent(
                this,
                Resources.getString("Validating") + "...",
                0, 0, 0));

        Collection<SearchResult> errors = new LinkedList<SearchResult>();

        this.validate(errors, l);

        boolean error = false;
        for (Iterator<SearchResult> it = errors.iterator(); it.hasNext() && !error;) {
            if (it.next().getType() == SearchResult.Type.ERROR) {
                error = true;
            }
        }

        if (error) {
            throw new FileSaveException(f, errors);
        }

        l.progressChanged(new ProgressEvent(this, Resources.getString("Writing") + "...", 0, 1, 0));

        XMLWriter out = null;
        File tmpFile = File.createTempFile("Save", ".xml", f.getParentFile());
        try {
            // write the document into a temporary file
            out = new XMLWriter(tmpFile);
            this.writeHeader(out);

            IdMap uid_map = new IdMap(true);
            this.write(out, uid_map);

            l.progressChanged(new ProgressEvent(
                    this,
                    Resources.getString("Writing") + "...",
                    0, 1, 1));

            out.close();
            out = null;

            // if everything worked okay, rename the temporary
            // file to the intended name
            boolean success = true;
            try {
                if (f.exists()) {
                    success = f.delete();
                }
                if (success) {
                    success = tmpFile.renameTo(f);
                }
            } catch (Exception exn) {
                success = false;
            }

            if (success) {
                this.file = f;
            } else {
                this.file = tmpFile;
                OptionPane.warning(parent, new String[]{
                    Resources.format("CouldNotSaveFile", f.getName()),
                    Resources.format("SavedFileAs", this.file.getName())});
            }

            // if isFullSave is true, clear the document's dirty status
            // and change the window title
            if (isFullSave) {
                setDirty(false);
                setTitle(this.file.getName());
            }
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    public void validate(Collection<SearchResult> errors, ProgressListener l) {

        // nothing (yet)
    }

    abstract protected String getDocumentType();

    abstract public void load(File f, XMLReader r);

    abstract public void write(XMLWriter out, IdMap uidmap);

    public void addPropertyChangeListener(PropertyChangeListener l) {

        this.pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {

        this.pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String property, Object oldValue,
            Object newValue) {

        this.pcs.firePropertyChange(property, oldValue, newValue);
    }

    public static Document load(File f) throws IOException {

        return new DocumentLoader(null).load(f, null);
    }

    public boolean isReadOnly() {

        return this.readOnly;
    }

    public void setReadOnly(boolean readOnly) {

        this.readOnly = readOnly;
    }
}
