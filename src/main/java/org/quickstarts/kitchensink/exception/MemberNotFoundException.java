package org.quickstarts.kitchensink.exception;

public class MemberNotFoundException extends Exception {
    public MemberNotFoundException() {
        super("Member not found");
    }
}
