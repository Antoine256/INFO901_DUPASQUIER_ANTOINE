package fr.usmb.distbidule;

public class DedicatedMessage extends Message{

    private final int dest;

    public DedicatedMessage(Object message, int dest){
        super(message);
        this.dest = dest;
    }

    @Override
    public String toString(){
        return ""+this.getMessage();
    }

    public int getDest(){
        return this.dest;
    }

}
