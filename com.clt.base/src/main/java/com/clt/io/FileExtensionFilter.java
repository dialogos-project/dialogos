package com.clt.io;

import java.io.File;

public class FileExtensionFilter
    extends javax.swing.filechooser.FileFilter
    implements java.io.FileFilter, java.io.FilenameFilter {

  private String extension;

  private String description;


  public FileExtensionFilter(String extension) {

    this(extension, null);
  }


  public FileExtensionFilter(String extension, String description) {

    if (extension.startsWith(".")) {
      this.extension = extension;
    }
    else {
      this.extension = "." + extension;
    }

    if (description != null) {
      this.description = description;
    }
    else {
      this.description = this.extension + " files";
    }
  }


  @Override
  public boolean accept(File file) {

    String fileName = file.getName();

    return file.isDirectory() || fileName.endsWith(this.description);
  }


  public boolean accept(File dir, String name) {

    return this.accept(new File(dir, name));
  }


  @Override
  public String getDescription() {

    return "*" + this.extension + " (" + this.description + ")";
  }

}