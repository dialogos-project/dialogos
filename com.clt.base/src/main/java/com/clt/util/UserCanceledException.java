package com.clt.util;

/**
 * Throwing a UserCanceledException indicates that the user has aborted an
 * action, in most cases by clicking a "Cancel" button in a dialog. The
 * exception doesn't carry any further information.
 *
 * @author Daniel Bobbert
 * @version 1.0
 */
public class UserCanceledException extends Exception {

    public UserCanceledException() {

        super();
    }
}
