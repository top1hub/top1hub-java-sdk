package top1hub.message;

public class ResourceException extends RuntimeException {
    public ResourceException(Exception e) {
        super(e);
    }

    public ResourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceException(Throwable cause) {
        super(cause);
    }

    protected ResourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ResourceException(String message) {
        super(message);
    }
}
