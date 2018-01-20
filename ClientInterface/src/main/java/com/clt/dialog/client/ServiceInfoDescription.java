package com.clt.dialog.client;

import javax.jmdns.ServiceInfo;

/**
 * 
 * 
 * @author Daniel Bobbert
 * @version 1.0
 */

public class ServiceInfoDescription {

  ServiceInfo service;
  boolean showName;


  public ServiceInfoDescription(ServiceInfo service) {

    this(service, false);
  }


  public ServiceInfoDescription(ServiceInfo service, boolean showName) {

    this.service = service;
    this.showName = showName;
  }


  public ServiceInfo getService() {

    return this.service;
  }


  public String getServiceName() {

    String clientName = this.service.getPropertyString("name");
    if (clientName != null) {
      return clientName;
    }
    else {
      return this.service.getName();
    }
  }


  public String getHostname() {

    String server = this.service.getServer();
    if (server.endsWith(".local.")) {
      server = server.substring(0, server.length() - ".local.".length());
    }
    return server;
  }


  public int getPort() {

    return this.service.getPort();
  }


  @Override
  public String toString() {

    StringBuilder b = new StringBuilder();
    if (this.showName) {
      b.append(this.getServiceName());
      b.append(" (");
    }
    b.append(this.getHostname());
    b.append(':');
    b.append(this.getPort());
    if (this.showName) {
      b.append(")");
    }
    return b.toString();
  }


  @Override
  public boolean equals(Object o) {

    if (o instanceof ServiceInfoDescription) {
      return ((ServiceInfoDescription)o).getService().equals(this.getService());
    }
    else {
      return false;
    }
  }


  @Override
  public int hashCode() {

    return this.getService().hashCode();
  }
}