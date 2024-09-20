package fr.usmb.distbidule.messages;

public class SyncBroadcastMessage extends Message{
    private int from;

    public SyncBroadcastMessage(int from, Object message){
        super(message);
        this.from = from;
    }

    //quand tu broadcast, tu attends que tout le monde ait reçu avec la méthode synchronized

    public int getFrom(){
        return this.from;
    }

    @Override
    public String toString() {
        return "";
    }
}
