package fr.usmb.distbidule;

import fr.usmb.distbidule.messages.Message;

import java.util.ArrayList;

public class BoitAuLettre {
    private ArrayList<Message> messages;

    public BoitAuLettre() {
        this.messages = new ArrayList<>();
    }

    public void addMessage(Message m){
        messages.add(m);
    }
}
