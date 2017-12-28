/**
 * 
 */

package com.clt.diamant.graph;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;

import com.clt.diamant.Mapping;
import com.clt.util.DefaultPropertyContainer;

/**
 * @author dabo
 * 
 */
public abstract class VisualGraphElement
    extends DefaultPropertyContainer<Object>
    implements GraphElement, GroupElement, MoveableElement, ColorizableElement {

  // do not change!! These are persistent property names that are written to
  // XML.
  public static final String COMMENT = "comment";
  public static final String LOCATION = "location";
  public static final String SIZE = "size";
  public static final String COLOR = "color";

  private Group group = null;


  /*
   * public VisualGraphElement() { super(false); }
   */

  public void setGroup(Group g) {

    this.group = g;
  }


  public Group getGroup() {

    return this.group;
  }


  public void setLocation(int x, int y) {

    this.setProperty(VisualGraphElement.LOCATION, new Point(x, y));
  }


  public Point getLocation() {

    return (Point)this.getProperty(VisualGraphElement.LOCATION);
  }


  public int getX() {

    return this.getLocation().x;
  }


  public int getY() {

    return this.getLocation().y;
  }


  public void setSize(int width, int height) {

    this.setProperty(VisualGraphElement.SIZE, new Dimension(width, height));
  }


  public Dimension getSize() {

    return (Dimension)this.getProperty(VisualGraphElement.SIZE);
  }


  public int getWidth() {

    return this.getSize().width;
  }


  public int getHeight() {

    return this.getSize().height;
  }


  public void setColor(Color c) {

    this.setProperty(VisualGraphElement.COLOR, c);
  }


  public Color getColor() {

    return (Color)this.getProperty(VisualGraphElement.COLOR);
  }


  public void setComment(String comment) {

    this.setProperty(VisualGraphElement.COMMENT, comment);
  }


  public String getComment() {

    return (String)this.getProperty(VisualGraphElement.COMMENT);
  }


  public abstract VisualGraphElement clone(Mapping map);


  public abstract void update(Mapping map);
}
