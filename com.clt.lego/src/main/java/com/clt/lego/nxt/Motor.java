package com.clt.lego.nxt;

import java.io.IOException;

public class Motor {

  public enum Port {
        A(0x00),
        B(0x01),
        C(0x02);

    private int id;


    private Port(int id) {

      this.id = id;
    }


    public int getID() {

      return this.id;
    }
  }

  public enum State {
        IDLE(0x00),
        RAMPUP(0x10),
        RUNNING(0x20),
        RAMPDOWN(0x40);

    private int value;


    private State(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  public enum Mode {
        ON(0x01),
        BRAKE(0x02),
        REGULATED(0x04);

    private int value;


    private Mode(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  public enum Regulation {
        IDLE(0x00),
        SPEED(0x01),
        SYNCHRONISED(0x02);

    private int value;


    private Regulation(int value) {

      this.value = value;
    }


    public int getValue() {

      return this.value;
    }
  }

  private Nxt brick;
  private Port port;
  private int power;


  public Motor(Nxt brick, Port port) {

    if (brick == null) {
      throw new IllegalArgumentException();
    }
    if (port == null) {
      throw new IllegalArgumentException();
    }
    this.brick = brick;
    this.port = port;
    this.power = 80;
  }


  public Port getPort() {

    return this.port;
  }


  public void setPower(int power) {

    if ((power < 0) || (power > 100)) {
      throw new IllegalArgumentException("Illegal value for NXT motor power");
    }
    this.power = power;
  }


  public void forward()
      throws IOException {

    this.brick.setOutputState(this.port, this.power, Mode.ON.getValue()
      + Mode.BRAKE.getValue()
                + Mode.REGULATED.getValue(), Regulation.SPEED, 0,
      State.RUNNING, 0);
  }


  public void forward(int power, final int degrees)
      throws IOException {

    this.setPower(power);
    this.brick.setOutputState(this.port, power, Mode.ON.getValue()
      + Mode.BRAKE.getValue()
                + Mode.REGULATED.getValue(), Regulation.SPEED, 0, State.RAMPUP,
      degrees);
    new Thread(new Runnable() {

      public void run() {

        try {
          // brick.setOutputState(port, power, Mode.ON.getValue() +
          // Mode.BRAKE.getValue()
          // + Mode.REGULATED.getValue(), Regulation.SPEED, 0, State.RAMPDOWN,
          // degrees);
          long time = System.currentTimeMillis();
          while ((Motor.this.brick.getOutputState(Motor.this.port)
            .getRotationCount() < degrees)
                            && (System.currentTimeMillis() < time + 10000)) {
            ;
          }
          Motor.this.stop();
        }
                catch (IOException exn) {
                  // TODO Auto-generated catch block
                  exn.printStackTrace();
                }
              }
    }).start();
  }


  public void backward()
      throws IOException {

    this.brick.setOutputState(this.port, -this.power, Mode.ON.getValue()
      + Mode.BRAKE.getValue()
                + Mode.REGULATED.getValue(), Regulation.SPEED, 0,
      State.RUNNING, 0);
  }


  public void backward(int power)
      throws IOException {

    this.setPower(power);
    this.backward();
  }


  public void stop()
      throws IOException {

    this.brick.setOutputState(this.port, 0, Mode.ON.getValue()
      + Mode.BRAKE.getValue()
                + Mode.REGULATED.getValue(), Regulation.SPEED, 0,
      State.RUNNING, 0);
  }


  public void drift()
      throws IOException {

    this.brick.setOutputState(this.port, 0, Mode.REGULATED.getValue(),
      Regulation.SPEED, 0, State.IDLE, 0);
  }


  public void resetPosition(boolean relative)
      throws IOException {

    this.brick.resetMotorPosition(this.port, relative);
  }


  public MotorState getState()
      throws IOException {

    return this.brick.getOutputState(this.port);
  }
}
