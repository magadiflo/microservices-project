package dev.magadiflo.authorization.server.app.exception;

public class CommunicationException extends RuntimeException {
    public CommunicationException(String message) {
        super(message);
    }
}
