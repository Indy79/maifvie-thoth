package fr.maifvie.thoth.example.application.models;

public class TransfertMetadata {

    private final String to;

    public TransfertMetadata() {
        this.to = null;
    }

    public TransfertMetadata(String to) {
        this.to = to;
    }

    public String getTo() {
        return to;
    }

}
