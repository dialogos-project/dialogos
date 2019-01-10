package com.clt.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.UnsupportedEncodingException;
import li.flor.nativejfilechooser.NativeJFileChooser;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.clt.io.FileExtensionFilter;
import com.clt.util.Platform;

public class FileChooser {

  public static boolean useSystemFileChooser = false;

  private static Frame awtparent;

  static {
    FileChooser.awtparent = new Frame();
    Dimension ScreenSize = WindowUtils.getScreenSize();
    FileChooser.awtparent.setLocation((ScreenSize.width - 350) / 2,
      (ScreenSize.height - 300) / 2);
  }

  private File currentdir;

  private FileFilter filter;

  public FileChooser() {
    this(new FileExtensionFilter("dos", "DialogOS dialog model"));
  }

  public FileChooser(FileExtensionFilter filter) {
    this.filter = filter;
    String workingDirectory = System.getProperty("user.dir");
    if (workingDirectory == null) {
      workingDirectory = ".";
    }
    this.currentdir = new File(workingDirectory);
  }

  public FileChooser(String currentDirectoryPath) {
    this();
    if (currentDirectoryPath != null) {
      this.currentdir = new File(currentDirectoryPath);
    }
  }


  public FileChooser(File currentDirectory) {
    this();
    if (currentDirectory != null) {
      this.currentdir = currentDirectory;
    }
  }


  public void setFileFilter(FileFilter filter) {
    this.filter = filter;
  }


  // **************************************************************************************************************
  // Datei laden
  // **************************************************************************************************************

  public File standardGetFile() {
    return this.standardGetFile(null);
  }


  public File standardGetFile(Component parent) {
    return this.standardGetFile(parent, null);
  }


  public File standardGetFile(Component parent, String title) {
    if (FileChooser.useSystemFileChooser) {
      FileDialog chooser =
        new FileDialog(FileChooser.awtparent, Platform.isMac() ? null
                    : (title == null ? GUI.getString("OpenFile") : title),
          FileDialog.LOAD);
      WindowUtils.installModalDialog(FileChooser.awtparent);
      chooser.setDirectory(this.currentdir.getPath());
      chooser.setVisible(true);
      String file = chooser.getFile(), dir = chooser.getDirectory();
      chooser.dispose();
      WindowUtils.deinstallModalDialog(FileChooser.awtparent);

      if ((file != null) && Platform.isMac()) {
        // Fix a bug in the MacOS X GM FileDialog, where paths are
        // returned as UTF.
        if (!new File(dir, file).exists()) {
          try {
            String newfile = new String(file.getBytes(), "UTF8"), newdir = null;
            if (dir != null) {
              newdir = new String(dir.getBytes(), "UTF8");
            }
            if (new File(newdir, newfile).exists()) {
              // Only change the file, if the original was not
              // found
              // but the UTF decoded file exists.
              file = newfile;
              dir = newdir;
            }
          } catch (UnsupportedEncodingException ignore) {
            // UTF8 should always be supported.
            // Simply ignore errors, this is a historical workaround anyways.
          }
        }
      }
      if (dir != null) {
        this.currentdir = new File(dir);
      }
      return (file == null ? null : new File(dir, file));
    }
    else {
      NativeJFileChooser chooser = new NativeJFileChooser(this.currentdir);
      chooser.setDialogTitle(title == null ? GUI.getString("OpenFile") : title);
      if (this.filter != null) {
        chooser.setFileFilter(this.filter);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("DialogOS dialog model", "dos"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setAcceptAllFileFilterUsed(true);
      }
      if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
        return null;
      }
      else {
        this.currentdir = chooser.getCurrentDirectory();
        return chooser.getSelectedFile();
      }
    }
  }


  public File standardGetFileOrFolder(Component parent, String title) {
    JFileChooser chooser = new NativeJFileChooser(this.currentdir);
    chooser.setDialogTitle(title == null ? GUI.getString("OpenFile") : title);
    chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
    if (this.filter != null) {
      chooser.setFileFilter(this.filter);
    }
    if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
      return null;
    }
    else {
      this.currentdir = chooser.getCurrentDirectory();
      return chooser.getSelectedFile();
    }
  }


  // **************************************************************************************************************
  // Verzeichnis laden
  // **************************************************************************************************************

  private boolean supportsFolderChooser() {
    if (Platform.isMac()) {
      try {
        new FileDialog(FileChooser.awtparent, "Test", 3).dispose();
        return true;
      } catch (Exception exn) {
        return false;
      }
    }
    else {
      return false;
    }
  }


  public File standardGetDir() {
    return this.standardGetDir(null);
  }


  public File standardGetDir(Component parent) {
    return this.standardGetDir(parent, null);
  }


  public File standardGetDir(Component parent, String title) {
    // Der AWT FolderChooser funktioniert nur auf dem Mac (type = 3)
    if (FileChooser.useSystemFileChooser && this.supportsFolderChooser()) {
      FileDialog chooser =
        new FileDialog(
          FileChooser.awtparent,
          Platform.isMac()
            ? null
                    : (title == null ? GUI.getString("ChooseDirectory") : title),
          3);
      WindowUtils.installModalDialog(FileChooser.awtparent);
      if (Platform.isMac()) {
        chooser.setFile(null);
        chooser.setDirectory(null);
      }
      else {
        chooser.setDirectory(this.currentdir.getPath());
      }
      chooser.setVisible(true);
      String file = chooser.getFile(), dir = chooser.getDirectory();
      chooser.dispose();
      WindowUtils.deinstallModalDialog(FileChooser.awtparent);

      if ((file == null) && (dir != null) && Platform.isMac()) {
        // Fix for the fact, that HFS dirs on Mac OS X are
        // sometimes returned in the 'dir' field with an empty file
        // field
        File ff = new File(dir);
        file = ff.getName();
        dir = ff.getParent();
      }

      if ((file != null) && Platform.isMac()) {
        // Fix a bug in the MacOS X GM FileDialog, where paths are
        // returned as UTF.
        if (!new File(dir, file).exists()) {
          try {
            String newfile = new String(file.getBytes(), "UTF8"), newdir = null;
            if (dir != null) {
              newdir = new String(dir.getBytes(), "UTF8");
            }
            if (new File(newdir, newfile).exists()) {
              // Only change the file, if the original was not
              // found
              // but the UTF decoded file exists.
              file = newfile;
              dir = newdir;
            }
          } catch (UnsupportedEncodingException exn) {
            // UTF8 should always be supported.
            // Simply ignore errors, this is a historical workaround anyways.
          }
        }
      }
      if (dir != null) {
        this.currentdir = new File(dir);
      }
      if (file == null) {
        return null;
      }
      else {
        File f = new File(dir, file);
        return f.isDirectory() ? f : new File(dir);
      }
    }
    else {
      JFileChooser chooser = new SwingFileChooser(this.currentdir);
      chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      chooser.setDialogTitle(title == null ? GUI.getString("OpenFile") : title);
      chooser.setApproveButtonText(GUI.getString("Choose"));
      if (this.filter != null) {
        chooser.addChoosableFileFilter(this.filter);
        //chooser.addChoosableFileFilter(new FileNameExtensionFilter("DialogOS dialog model", "dos"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(this.filter);
      }
      if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
        return null;
      }
      else {
        this.currentdir = chooser.getCurrentDirectory();
        return chooser.getSelectedFile();
      }
    }
  }


  // **************************************************************************************************************
  // Datei speichern
  // **************************************************************************************************************

  public File standardPutFile() {
    return this.standardPutFile(null);
  }


  public File standardPutFile(String filename) {
    return this.standardPutFile(null, filename);
  }


  public File standardPutFile(Component parent, String filename) {
    return this.standardPutFile(parent, filename, null);
  }


  public File standardPutFile(Component parent, String filename, String title) {
    if (FileChooser.useSystemFileChooser) {
      FileDialog chooser =
        new FileDialog(FileChooser.awtparent, title == null ? GUI
          .getString("SaveAs")
                    : title, FileDialog.SAVE);
      WindowUtils.installModalDialog(FileChooser.awtparent);
      chooser.setDirectory(this.currentdir.getPath());
      if (filename != null) {
        chooser.setFile(filename);
      }
      chooser.setVisible(true);
      String file = chooser.getFile(), dir = chooser.getDirectory();
      chooser.dispose();
      WindowUtils.deinstallModalDialog(FileChooser.awtparent);
      if (dir != null) {
        this.currentdir = new File(dir);
      }
      return (file == null ? null : new File(dir, file));
    }
    else {
      JFileChooser chooser = new NativeJFileChooser(this.currentdir);
      if (filename != null) {
        chooser.setSelectedFile(new File(this.currentdir, filename));
      }
      chooser.setDialogTitle(title == null ? GUI.getString("SaveAs") : title);
      if (this.filter != null) {
        chooser.setFileFilter(this.filter);
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("DialogOS dialog model", "dos"));
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setAcceptAllFileFilterUsed(true);
      }

      if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
        return null;
      }
      else {
        this.currentdir = chooser.getCurrentDirectory();
        return chooser.getSelectedFile();
      }
    }
  }

  // ACHTUNG: Um einige Bugs im JRE 1.2 zu umgehen, veraendert der
  // SwingFileChooser den
  // FileSelectionMode, ohne ihn zurueckzusetzen.
  // Wenn man eine langlebige Instanz behaelt, muss man also jedesmal neu
  // setFileSelectionMode() aufrufen.
  private static class SwingFileChooser
        extends NativeJFileChooser {

    boolean dirspecial = false;

    public SwingFileChooser() {
      super();
    }

    public SwingFileChooser(File dir) {
      super(dir);
    }


    @Override
    public int showDialog(Component parent, String approveButtonText) {
      int result;

      // Sch.. auf den parent. Das macht der AWTDialog ja auch, und sonst
      // klappt
      // das managen der modalen Ebene nicht
      Frame tmpFrame = WindowUtils.createTempModalFrame();

      if (this.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
        this.dirspecial = true;
        this.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        result = super.showDialog(tmpFrame, approveButtonText);
      }
      else {
        result = super.showDialog(tmpFrame, approveButtonText);
      }

      tmpFrame.dispose();
      return result;
    }


    @Override
    public void approveSelection() {
      File f = this.getSelectedFile();

      if ((f == null) || (f.getName().length() == 0)) {
        return; // nichts ausgewaehlt. Das wollen wir nicht
      }

      if (this.getDialogType() == JFileChooser.OPEN_DIALOG) {
        if (this.dirspecial && f.isDirectory()) {
          this.setCurrentDirectory(f);
          this.setSelectedFile(null);
          return;
        }
      }
      else if (this.getDialogType() == JFileChooser.SAVE_DIALOG) {
        if (f.exists()) { // Datei ueberschreiben?
          if (OptionPane.confirm(this, GUI.getString("TheFile") + ' ' + f + ' '
                            + GUI.getString("existsWantToReplace?"), GUI
            .getString("ReplaceFile?"),
                        OptionPane.YES_NO_OPTION) != OptionPane.YES) {
            return; // nicht ersetzen?! Dann machen wir den Dialog
            // auch nicht zu!
          }
        }
      }

      // alles OK, also Auswahl akzeptieren
      super.approveSelection();
    }

  }
}
