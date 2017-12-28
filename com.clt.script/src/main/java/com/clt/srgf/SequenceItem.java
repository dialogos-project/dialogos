/*
 * @(#)SequenceItem.java
 * Created on 20.02.2007 by dabo
 *
 * Copyright (c) CLT Sprachtechnologie GmbH.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of CLT Sprachtechnologie GmbH ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with CLT Sprachtechnologie GmbH.
 */

package com.clt.srgf;

import java.util.List;

/**
 * @author dabo
 * 
 */
class SequenceItem
    extends Item {

  private Sequence sequence;
  private int position;


  public SequenceItem(Sequence sequence, int repeatMin, int repeatMax) {

    super(repeatMin, repeatMax);

    this.sequence = sequence;
    this.position = 0;
  }


  private SequenceItem(SequenceItem item) {

    super(item);

    this.sequence = item.sequence;
    this.position = item.position;
  }


  @Override
  public Item copy() {

    return new SequenceItem(this);
  }


  @Override
  public List<Parse> shift_(Parse p) {

    if (this.position == this.sequence.size()) {
      this.setDone(true);
    }
    else {
      p.push(this.sequence.get(this.position).createInstance());
      this.position++;
      if (this.position == this.sequence.size()) {
        this.setDone(true);
      }
    }
    return Item.itemList(p);
  }


  @Override
  public void reset() {

    super.reset();
    this.position = 0;
  }
}
