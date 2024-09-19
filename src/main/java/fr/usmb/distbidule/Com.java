package fr.usmb.distbidule;

import com.google.common.eventbus.Subscribe;

public class Com {
    private EventBusService bus;
    private int horloge;
    private BoitAuLettre bal;
    public static final int maxNbProcess = 3;
    private Process process;
    private static int nbProcess = 0;
    private int id = nbProcess++;
    private State tokenState = State.Null;
    private BoitAuLettre mailbox;

    Com(Process p){
        this.horloge = 0;
        this.bus = EventBusService.getInstance();
        this.bus.registerSubscriber(this); // Auto enregistrement sur le bus afin que les methodes "@Subscribe" soient invoquees automatiquement.
        this.process = p;
        if(id==nbProcess-1){
            initToken();
        }
        this.mailbox = new BoitAuLettre();
    }

   public void inc_clock() {
        this.horloge++;
    }

    public void sendTo(Object o,int to ){
        this.horloge++;
        DedicatedMessage message = new DedicatedMessage(o, to);
        message.setEstampillage(this.horloge);
        bus.postEvent(message);
    }

//    public void sendToSync(Object o,int to ){
//        this.horloge++;
//        DedicatedMessage message = new DedicatedMessage(o, to);
//        message.setEstampillage(this.horloge);
//        bus.postEvent(message);
//    }

    public void broadcast(Object o){
        BroadcastMessage b = new BroadcastMessage(o, this.getId());
        this.horloge++;
        b.setEstampillage(this.horloge);
        bus.postEvent(b);
    }

    public void freeBus(){
        this.bus.unRegisterSubscriber(this);
        this.bus = null;
    }

    // Declaration de la methode de callback invoqué lorsqu'un message de type Bidule transite sur le bus
    @Subscribe
    public void onReceive(DedicatedMessage b){
        if (b.getDest() == this.getId()){
            //ajouter à la BAL
            //je met à jour mon horloge de lamport
            mailbox.addMessage(b);
            if (b.getEstampillage() > this.horloge){
                this.horloge = b.getEstampillage();
            }
            this.horloge++;
            System.out.println(Thread.currentThread().getName() + " receives: " + b.getMessage() + " for " + this.process.getName());
            System.out.println(Thread.currentThread().getName() + " horloge after receive : " + this.horloge);
        }
    }

    @Subscribe
    public void onBroadcast(BroadcastMessage b){
        //je met à jour mon horloge de lamport
        if (b.getNbProcess() != this.getId()) {
            //ajouter à la boite aux lettres
            mailbox.addMessage(b);
            if (b.getEstampillage() > this.horloge){
                this.horloge = b.getEstampillage();
            }
            this.horloge++;
            System.out.println(Thread.currentThread().getName() + " receives broadcast: " + b.getMessage() + " for " + this.process.getName());
            System.out.println(Thread.currentThread().getName() + " horloge after receive : " + this.horloge);
        }
    }

    @Subscribe
    public void onToken(TokenMessage b){
        if (process.isAlive()){ 
            //je met à jour mon horloge de lamport
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

//    @Subscribe
//    public void recevFromSync(SyncMessage b){
//        //je met à jour mon horloge de lamport
//        if (b.getDest() == this.process.getId()){
//            if (b.getEstampillage() > this.horloge){
//                this.horloge = b.getEstampillage();
//            }
//            this.horloge++;
//            System.out.println(Thread.currentThread().getName() + " receives broadcast: " + b.getMessage() + " for " + this.process.getName());
//            System.out.println(Thread.currentThread().getName() + " horloge after receive : " + this.horloge);
//        }
//    }

    public int getId(){
        return this.id;
    }

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

    public void releaseSC(){
        tokenState = State.Release;
    }

    public void initToken(){
        Token token = new Token("Section critique");
        TokenMessage message = new TokenMessage(token, (id+1)%maxNbProcess);
        bus.postEvent(message);
    }
}
