package top1hub.message;

public class Result {
    private final int statusCode;
    private boolean success = false;
    private String message;

    public Result(boolean success, int statusCode, String message) {
        this.success = success;
        this.statusCode = statusCode;
        this.message = message;
    }

    public boolean success() {
        return this.success;
    }

    @Override
    public String toString() {
        return "Result{" +
                "statusCode=" + statusCode +
                ", success=" + success +
                ", message='" + message + '\'' +
                '}';
    }
}
