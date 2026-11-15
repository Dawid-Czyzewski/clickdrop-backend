package pl.clickdrop.gateway;

public class UnknownRouteException extends RuntimeException {
    public UnknownRouteException(String path) {
        super("No upstream for path: " + path);
    }
}
