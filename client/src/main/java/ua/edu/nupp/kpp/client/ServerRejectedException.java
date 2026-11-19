package ua.edu.nupp.kpp.client;

public class ServerRejectedException extends IllegalStateException {

    public ServerRejectedException(String message) {
        super(message);
    }
}
