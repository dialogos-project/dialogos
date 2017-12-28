package com.clt.diamant;

import com.clt.dialog.client.ServerDevice;

public class Device
    extends ServerDevice
    implements IdentityObject {

  private String id;


  public Device() {

    this(Resources.getString("Untitled"));
  }


  public Device(String name) {

    this(null, name);
  }


  public Device(String id, String name) {

    super(name, null);

    this.id = id;
  }


  public String getId() {

    return this.id;
  }


  public void setId(String id) {

    this.id = id;
  }


  public String getInfo() {

    if (this.getConnector() != null) {
      return this.getName() + " (" + this.getConnector().getInfo() + ")";
    }
    else {
      return this.getName() + " (" + Resources.getString("noConnector") + ")";
    }
  }


  @Override
  public String toString() {

    return this.getName();
  }

}