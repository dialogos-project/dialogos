package com.clt.mac;

import java.util.Hashtable;

interface AppleEventHandler {

    @SuppressWarnings("unchecked")
    public Object handleEvent(Object directObject, Hashtable parameters);
}
