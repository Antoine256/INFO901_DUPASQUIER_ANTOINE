package fr.usmb.distbidule;

import com.google.common.eventbus.Subscribe;
import fr.usmb.distbidule.messages.*;

import java.util.concurrent.Semaphore;

public class Com {
    private EventBusService bus;
    private Semaphore semaphore;
    private int horloge;
    private BoitAuLettre bal;
    public static final int maxNbProcess = 3;
    private Process process;
    private static int nbProcess = 0;
    private int id = nbProcess++;
    private State tokenState = State.Null;
    private BoitAuLettre mailbox;
    private boolean lock = true;

    Com(Process p){
        this.setClock(0);
        this.bus = EventBusService.getInstance();
        this.bus.registerSubscriber(this); // Auto enregistrement sur le bus afin que les methodes "@Subscribe" soient invoquees automatiquement.
        this.process = p;
        if(id==nbProcess-1){
            initToken();
        }
        this.semaphore = new Semaphore(1);
        this.mailbox = new BoitAuLettre();
    }

    // ENVOIE DE MESSAGES //


    /**
     * Envoie un message à un processus
     * @param o {@link Object} message à envoyer
     * @param to {@link int} id du processus à qui envoyer le message
     */
    public void sendTo(Object o,int to ){
        addTimetoClock(1);
        DedicatedMessage message = new DedicatedMessage(o, to);
        message.setEstampillage(this.horloge);
        bus.postEvent(message);
    }

    /**
     * Envoie un message à un processus et attend la réponse
     * @param o {@link Object} message à envoyer
     * @param to {@link int} id du processus à qui envoyer le message
     */
    public void sendToSync(Object o,int to ){
        SynchronizeMessage message = new SynchronizeMessage(this.getId(), to, SynchronizeMessageType.SendTo, o);
        message.setEstampillage(this.horloge);
        bus.postEvent(message);
        //On attend le message comme quoi il l'a bien recu
        lock = true;
        while (lock){
            try{
                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(this.getId()+" Synchronisé en sendTo avec "+to);
    }

    /**
     * Envoie un message à un processus et attend la réponse pour être synchronisé
     * @param o {@link Object} message à envoyer
     * @param from {@link int} id du processus de qui on attend le message
     */
    public void recvFromSync(Object o,int from ){
        //On attend le message de from
        lock = true;
        while (lock){
            try{
                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        System.out.println(this.getId()+" Synchronisé en Recv avec "+from);
    }

    /**
     * Envoie un message à un processus et attend la réponse pour être synchronisé
     * @param o {@link Object} message à envoyer
     * @param from {@link int} id du processus à qui envoyer le message
     */
    public void broadcastSync(Object o, int from){
        if (from == this.getId()){
            SyncBroadcastMessage message = new SyncBroadcastMessage(this.getId(), o);
            message.setEstampillage(this.horloge);
            bus.postEvent(message);
            //attendre que tout le monde ait reçu le message
            lock = true;
        }else{
            //attente du message de from
            while(lock){
                try{
                    Thread.sleep(500);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("Synchronisé en broadcast");
        }
    }

    public void synchronize(){

    }

    /**
     * Envoie un message à tous les processus
     * @param o {@link Object} message à envoyer
     */
    public void broadcast(Object o){
        BroadcastMessage b = new BroadcastMessage(o, this.getId());
        addTimetoClock(1);
        b.setEstampillage(this.horloge);
        bus.postEvent(b);
    }

    // RECEPTION DE MESSAGES //

    /**
     * Reception d'un message
     * @param b {@link DedicatedMessage} message reçu
     */
    @Subscribe
    public void onReceive(DedicatedMessage b){
        if (b.getDest() == this.getId()){
            //ajouter à la BAL
            //je met à jour mon horloge de lamport
            mailbox.addMessage(b);
            if (b.getEstampillage() > this.horloge){
                setClock(b.getEstampillage());
            }
            addTimetoClock(1);
            System.out.println(Thread.currentThread().getName() + " receives: " + b.getMessage() + " for " + this.process.getName());
            System.out.println(Thread.currentThread().getName() + " horloge after receive : " + this.horloge);
        }
    }

    /**
     * Reception d'un message de synchronisation
     * @param b {@link SynchronizeMessage} message reçu
     */
    @Subscribe
    public void onBroadcast(BroadcastMessage b){
        //je met à jour mon horloge de lamport
        if (b.getNbProcess() != this.getId()) {
            //ajouter à la boite aux lettres
            mailbox.addMessage(b);
            if (b.getEstampillage() > this.horloge){
                setClock(b.getEstampillage());
            }
            addTimetoClock(1);
            System.out.println(Thread.currentThread().getName() + " receives broadcast: " + b.getMessage() + " for " + this.process.getName());
            System.out.println(Thread.currentThread().getName() + " horloge after receive : " + this.horloge);
        }
    }

    @Subscribe
    public void receiveSync(SynchronizeMessage o) {
        //si c'est un sendto et que je suis pas le destinataire, je ne fais rien. sinon, je renvoie un message de confirmation (voir pour le recv comment ca marche.)
        //si c'est un message de confirmation, j'agit seulement si je suis le dest, je met à jour ma variable. sinon je ne fais rien
        if (this.getId() == o.getDest()) {
            //Je suis la destination
            if (o.getType() == SynchronizeMessageType.SendTo) {
                //Je suis la destination d'un sendTo donc je renvoie un message de confirmation
                //je met à jour mon horloge de lamport
                mailbox.addMessage(o);
                if (o.getEstampillage() > this.horloge) {
                    setClock(o.getEstampillage());
                }
                this.addTimetoClock(1);
                SynchronizeMessage message = new SynchronizeMessage(this.getId(), o.getFrom(), SynchronizeMessageType.Response, o.getMessage());
                message.setEstampillage(this.horloge);
                bus.postEvent(message);
            }
            if (o.getType() == SynchronizeMessageType.Response) {
                //Je suis la destination d'un message de confirmation
                //je met à jour mon horloge de lamport
                mailbox.addMessage(o);
                if (o.getEstampillage() > this.horloge) {
                    setClock(o.getEstampillage());
                }
                this.addTimetoClock(1);
                lock = false;
            }
            if (o.getType() == SynchronizeMessageType.Recv) {
                mailbox.addMessage(o);
                if (o.getEstampillage() > this.horloge) {
                    setClock(o.getEstampillage());
                }
                this.addTimetoClock(1);
                SynchronizeMessage message = new SynchronizeMessage(this.getId(), o.getFrom(), SynchronizeMessageType.Response, o.getMessage());
                message.setEstampillage(this.horloge);
                bus.postEvent(message);
            }
        }
    }

    /**
     * Reception d'un message de synchronisation en broadcast
     * @param b {@link SyncBroadcastMessage} message reçu
     */
    @Subscribe
    public void onSyncBroadcastReceive(SyncBroadcastMessage b){
        //je met à jour mon horloge de lamport
        if (b.getFrom() != this.getId()) {
            //ajouter à la boite aux lettres
            mailbox.addMessage(b);
            if (b.getEstampillage() > this.horloge){
                setClock(b.getEstampillage());
            }
            addTimetoClock(1);
        }
    }

    /**
     * Reception du token
     * @param b {@link TokenMessage} message reçu
     */
    @Subscribe
    public void onToken(TokenMessage b){
        if (process.isAlive()){
            //si on en a besoin, on le garde
            System.out.println("Je suis "+this.process.getName()+" j'ai recu le token " + (tokenState == State.Request ? " BESOIN " : " PAS BESOIN "));
            if (tokenState == State.Request){
                tokenState = State.SC;
                while (tokenState != State.Release){
                    try{
                        Thread.sleep(500);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }else if (tokenState != State.Null){
                System.out.println("Token's state of"+ this.process.getName() +" is incorrect !");
            }
            TokenMessage message = new TokenMessage(b.getToken(), (id+1)%maxNbProcess);
            bus.postEvent(message);
        }
    }


    // OTHERS //

    public void freeBus(){
        this.bus.unRegisterSubscriber(this);
        this.bus = null;
    }

    public int getId(){
        return this.id;
    }

    /**
     * Attend la section critique
     */
    public void requestSC(){
        if (tokenState == State.Null){
            tokenState = State.Request;
        }
        while(tokenState != State.SC){
            try{
                Thread.sleep(500);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Libère la section critique
     */
    public void releaseSC(){
        tokenState = State.Release;
    }

    /**
     * Initialise le token
     */
    public void initToken(){
        Token token = new Token("Section critique");
        TokenMessage message = new TokenMessage(token, (id+1)%maxNbProcess);
        bus.postEvent(message);
    }

    /**
     * Ajoute du temps à l'horloge de Lamport
     * @param time {@link int} temps à ajouter
     */
    public void addTimetoClock(int time){
        semaphore.tryAcquire();
        horloge += time;
        semaphore.release();
    }

    /**
     * Met à jour l'horloge de Lamport
     * @param time {@link int} temps à mettre
     */
    public void setClock(int time){
        semaphore.tryAcquire();
        horloge = time;
        semaphore.release();
    }
}
