package fr.usmb.distbidule;

public class BroadcastMessage extends Message {

    private final int source;

    public BroadcastMessage(Object message, int source) {
        super(message);
        this.source = source;
    }

    @Override
    public String toString() {
        return ""+ this.getMessage();
    }

    public int getNbProcess() {
        return this.source;
    }
}
