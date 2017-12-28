package com.clt.diamant;

public class DialogInput<T> {

  private Device device;
  private T input;


  public DialogInput(Device device, T input) {

    this.device = device;
    this.input = input;
  }


  public Device getDevice() {

    return this.device;
  }


  public T getInput() {

    return this.input;
  }
}