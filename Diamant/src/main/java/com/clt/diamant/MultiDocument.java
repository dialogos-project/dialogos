package com.clt.diamant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.clt.xml.AbstractHandler;
import com.clt.xml.XMLReader;
import com.clt.xml.XMLWriter;

public class MultiDocument
    extends Document {

  private List<File> documents;


  public MultiDocument() {

    this.documents = new ArrayList<File>();
  }


  public List<File> getDocuments() {

    return this.documents;
  }


  @Override
  public String getDocumentType() {

    return "Experiment";
  }


  @Override
  public void load(File file, XMLReader r) {

    this.documents.clear();
    r.setHandler(new AbstractHandler("experiment") {

      @Override
      public void end(String name) {

        if (name.equals("file")) {
          MultiDocument.this.documents.add(new File(this.getValue()));
          MultiDocument.this.firePropertyChange("documents",
            MultiDocument.this.documents, MultiDocument.this.documents);
        }
      }
    });

    this.setFile(file);
  }


  @Override
  public void write(XMLWriter out, IdMap uidmap) {

    out.openElement("experiment");

    for (int i = 0; i < this.documents.size(); i++) {
      out.printElement("file", this.documents.get(i).getAbsolutePath());
    }

    out.closeElement("experiment");
  }

}