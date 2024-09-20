package fr.usmb.distbidule.messages;

public class StringMessage extends Message<String> {
    public StringMessage(String message){
        super(message);
    }

    public String toString(){
        return this.getMessage();
    }
}
