package org.uniupo.it.model;

public class DisplayMessageFormat {
    private boolean isError;
    private String message;

    public DisplayMessageFormat(boolean isError, String message) {
        this.isError = isError;
        this.message = message;
    }

    public boolean isError() {
        return isError;
    }

    public void setError(boolean isError) {
        this.isError = isError;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String toString() {
        return "MessageFormat{isError=" + isError + ", message=" + message + "}";
    }
}
