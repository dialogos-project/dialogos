package com.clt.mac;

class MacError extends Exception {

    public MacError() {

        super();
    }

    public MacError(String message) {

        super(message);
    }

    public MacError(int error) {

        super("MacOS Error " + error + ": " + MacError.getErrorMessage(error));
    }

    private static String getErrorMessage(int error) {

        switch (error) {

            default:
                return "Unknown error";
        }
    }
}
