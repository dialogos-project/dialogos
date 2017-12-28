/**
 * 
 */
package com.clt.diamant.graph.ui;

import java.awt.event.MouseEvent;

/**
 * @author dabo
 * 
 */
public interface MouseHandler {

  /** @return whether the event was handled */
  public boolean mousePressed(MouseEvent evt);


  public void mouseReleased(MouseEvent evt);


  public void mouseDragged(MouseEvent evt);
}
