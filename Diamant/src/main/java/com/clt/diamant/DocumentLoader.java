package com.clt.diamant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import com.clt.event.ProgressEvent;
import com.clt.event.ProgressListener;
import com.clt.gui.ProgressDialog;
import com.clt.util.DefaultLongAction;
import com.clt.util.LongAction;
import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLProgressListener;
import com.clt.xml.XMLReader;

class DocumentLoader {

  private Document d;


  public DocumentLoader(Document d) {

    this.d = d;
  }


  /**
   * Loads a new document from a file.
   * 
   * @param f
   *          File to load
   * @param progress
   *          ProgressListener
   * @return The document obtained by loading the file.
   */
  public Document load(final File f, ProgressListener progress)
      throws IOException {

    final XMLReader r = new XMLReader(Document.validateXML);
    try {
      String description = Resources.format("Loading", f.getName());
      LongAction loading = new LoadingAction(description, r, f);

      if (progress == null) {
        new ProgressDialog(null).run(loading);
      }
      else {
        loading.addProgressListener(progress);
        progress
            .progressChanged(new ProgressEvent(this, loading.getDescription(),
              0, 0, 0));
        try {
          loading.run();
        } finally {
          loading.removeProgressListener(progress);
        }
      }
    } catch (InvocationTargetException exn) {
      if (exn.getTargetException() instanceof IOException) {
        throw (IOException)exn.getTargetException();
      }
      else if (exn.getTargetException() instanceof RuntimeException) {
        throw (RuntimeException)exn.getTargetException();
      }
      else {
        throw new IOException(exn.getTargetException().toString(), exn.getTargetException());
      }
    } catch (Exception exn) {
      if (exn instanceof IOException) {
        throw (IOException)exn;
      }
      else if (exn instanceof RuntimeException) {
        throw (RuntimeException)exn;
      }
      else {
        throw new IOException(exn.toString());
      }
    }

    System.out.println("End Of: DocumentLoader::load");
    return this.d;
  }

  /**
   * This Action loads a document and displays a progressbar while doing so.
   */
  class LoadingAction extends DefaultLongAction {

    private XMLReader r;
    private File f;


    public LoadingAction(String description, XMLReader r, File f) {

      super(description);
      this.r = r;
      this.f = f;
    }


    @Override
    public void run(final ProgressListener l)
        throws IOException {

      if (l != null) {
        final ProgressEvent evt =
          new ProgressEvent(DocumentLoader.this, this.getDescription()
            + "...", 0, 400, 0);
        XMLProgressListener progress = new XMLProgressListener() {

          public void percentComplete(float percent) {

            evt.setCurrent((int)(evt.getEnd() * percent));

            // invoked because progress was made.
            l.progressChanged(evt);
          }
        };
        this.r.addProgressListener(progress);
      }
      this.r.parse(this.f, new AbstractHandler() {

        @Override
        public void start(String name, Attributes atts)
            throws SAXException {

          System.out.println("  LoadingAction::start::name: " + name);
          if (name.equals("wizard")) {
            if (DocumentLoader.this.d == null) {
              System.out.println("  LoadingAction::start - creating"
                  + " new single document");
              DocumentLoader.this.d = new SingleDocument();
              System.out.println("  LoadingAction::start - done "
                  + "creating new single document");
            }
            else if (!(DocumentLoader.this.d instanceof SingleDocument)) {
              LoadingAction.this.r.raiseException(Resources
                .getString("DocumentTypeChanged"));
            }
            System.out.println("  LoadingAction::start - loading");
            // here a new start node is created
            DocumentLoader.this.d.load(LoadingAction.this.f,
              LoadingAction.this.r);
            System.out.println("LoadingAction::start - done loading");
          }
          else if (name.equals("log")) {
            System.out.println("log");
            if (DocumentLoader.this.d == null) {
              DocumentLoader.this.d = new LogDocument();
            }
            else if (!(DocumentLoader.this.d instanceof LogDocument)) {
              LoadingAction.this.r.raiseException(Resources
                .getString("DocumentTypeChanged"));
            }
            DocumentLoader.this.d.load(LoadingAction.this.f,
              LoadingAction.this.r);
          }
          else if (name.equals("experiment")) {
            System.out.println("experiment");
            if (DocumentLoader.this.d == null) {
              DocumentLoader.this.d = new MultiDocument();
            }
            else if (!(DocumentLoader.this.d instanceof MultiDocument)) {
              LoadingAction.this.r.raiseException(Resources
                .getString("DocumentTypeChanged"));
            }
            DocumentLoader.this.d.load(LoadingAction.this.f,
              LoadingAction.this.r);
          }
          else {
            System.out.println("raise exception");
            LoadingAction.this.r.raiseException(Resources
              .getString("UnknownDocumentType"));
          }
        }
      });
      System.out.println("LoadingAction::run  - end");
    }
  }

}
