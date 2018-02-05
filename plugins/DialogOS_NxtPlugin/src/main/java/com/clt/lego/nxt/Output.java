package com.clt.lego.nxt;

import java.io.IOException;

/**
 * @author dabo
 *
 */
public class Output extends Module {

    // The Output module stores the state of each output in 32 bytes.
    // The respective byte for port x is thus 32*x + offset
    private static final int OFFSET_TACHO_COUNT = 0;
    private static final int OFFSET_BLOCK_TACHO_COUNT = 4;
    private static final int OFFSET_ROTATION_COUNT = 8;
    private static final int OFFSET_TACHO_LIMIT = 12;
    private static final int OFFSET_MOTOR_RPM = 16;
    private static final int OFFSET_UPDATE_FLAGS = 18;
    private static final int OFFSET_MODE = 19;
    private static final int OFFSET_SPEED = 20;
    private static final int OFFSET_ACTUAL_SPEED = 21;
    private static final int OFFSET_PROPORTIONAL_PID = 22;
    private static final int OFFSET_INTEGRAL_PID = 23;
    private static final int OFFSET_DERIVATIVE_PID = 24;
    private static final int OFFSET_STATE = 25;
    private static final int OFFSET_REGULATION_MODE = 26;
    private static final int OFFSET_OVERLOADED = 27;
    private static final int OFFSET_SYNC_TURN_PARAMETER = 28;

    // common to all motors
    private static final int OFFSET_MODULATION_FREQUENCY = 96;

    public Output(Nxt brick) throws IOException {
        super(brick, "Output.mod");
    }

    public int getTachoCount(Motor.Port port) throws IOException {
        return (int) this.readNum(port.getID() * 32 + Output.OFFSET_TACHO_COUNT, 4, false);
    }

    public int getBlockTachoCount(Motor.Port port) throws IOException {
        return (int) this.readNum(port.getID() * 32 + Output.OFFSET_BLOCK_TACHO_COUNT, 4, false);
    }

    public int getRotationCount(Motor.Port port) throws IOException {
        return (int) this.readNum(port.getID() * 32 + Output.OFFSET_ROTATION_COUNT, 4, false);
    }

    public long getTachoLimit(Motor.Port port) throws IOException {
        return this.readNum(port.getID() * 32 + Output.OFFSET_TACHO_LIMIT, 4, false);
    }

    public int getTurnRatio(Motor.Port port) throws IOException {
        return (byte) this.readNum(port.getID() * 32 + Output.OFFSET_SYNC_TURN_PARAMETER, 1, false);
    }

    public int getPower(Motor.Port port) throws IOException {
        return (byte) this.readNum(port.getID() * 32 + Output.OFFSET_SPEED, 1, false);
    }

    public int getActualPower(Motor.Port port) throws IOException {
        return (byte) this.readNum(port.getID() * 32 + Output.OFFSET_ACTUAL_SPEED, 1, false);
    }

    public int getRPM(Motor.Port port) throws IOException {
        return (int) this.readNum(port.getID() * 32 + Output.OFFSET_MOTOR_RPM, 2, false);
    }

    public boolean isOverloaded(Motor.Port port) throws IOException {
        return this.readNum(port.getID() * 32 + Output.OFFSET_OVERLOADED, 1, false) != 0;
    }

    public Motor.State getState(Motor.Port port) throws IOException {
        int value = (int) this.readNum(port.getID() * 32 + Output.OFFSET_STATE, 1, false);

        for (Motor.State s : Motor.State.values()) {
            if (s.getValue() == value) {
                return s;
            }
        }

        return null;
    }

    public Motor.Regulation getRegulation(Motor.Port port) throws IOException {
        int value = (int) this.readNum(port.getID() * 32 + Output.OFFSET_REGULATION_MODE, 1, false);

        for (Motor.Regulation r : Motor.Regulation.values()) {
            if (r.getValue() == value) {
                return r;
            }
        }

        return null;
    }

    public int getPulseWidthModulationFrequency() throws IOException {
        return (int) this.readNum(Output.OFFSET_MODULATION_FREQUENCY, 1, false);
    }
}
