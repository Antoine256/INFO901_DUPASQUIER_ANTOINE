package fr.usmb.distbidule.messages;

public class SynchronizeMessage extends Message {
    private int from;
    private int dest;
    private SynchronizeMessageType type;

    public SynchronizeMessage(int from, int dest, SynchronizeMessageType type, Object message){
        super(message);
        this.from = from;
        this.dest = dest;
        this.type = type;
    }

    //quand tu broacast, tu attend que tout le monde ait reçu avec la méthode synchronized
    //quand tu send à qqun pareil tu attend qu'il ait reçu
    //quand tu receive from sync tu attend le message de from

    @Override
    public String toString() {
        return "";
    }

    public int getFrom(){
        return this.from;
    }

    public int getDest(){
        return this.dest;
    }
    public SynchronizeMessageType getType(){
        return this.type;
    }
}
