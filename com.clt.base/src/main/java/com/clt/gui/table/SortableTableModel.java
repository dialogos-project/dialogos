/*
 * @(#)SortableTableModel.java
 * Created on 22.02.2006 by dabo
 *
 * Copyright (c) 2006 CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.gui.table;

import javax.swing.table.TableModel;

/**
 * @author dabo
 * 
 */
public interface SortableTableModel
    extends TableModel {

  enum SortMode {
        NONE,
        ASCENDING,
        DESCENDING
  }


  public SortMode getSortMode(int column);
}
