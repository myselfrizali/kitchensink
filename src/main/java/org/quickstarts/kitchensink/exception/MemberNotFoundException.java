package org.quickstarts.kitchensink.exception;

public class MemberNotFoundException extends RuntimeException {
    public MemberNotFoundException() {
        super("Member not found or deleted");
    }

    public MemberNotFoundException(String message) {
        super(message);
    }
}
