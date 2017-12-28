/**
 * 
 */
package com.clt.diamant.graph;

/**
 * @author dabo
 * 
 */
public interface MoveableElement {

  public int getX();


  public int getY();


  public int getWidth();


  public int getHeight();


  public void setLocation(int x, int y);


  public void setSize(int width, int height);
}
