package com.clt.diamant.log;

class InputLogEvent extends LogEvent<String> {

    private boolean logOnly = false;

    public InputLogEvent(int time, String device, String value, boolean logOnly) {

        super(device, time, new String[]{value});

        this.logOnly = logOnly;
    }

    public boolean logOnly() {

        return this.logOnly;
    }
}
