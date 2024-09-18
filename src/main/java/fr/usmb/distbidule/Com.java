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

    Com(Process p){
        this.horloge = 0;
        this.bus = EventBusService.getInstance();
        this.bus.registerSubscriber(this); // Auto enregistrement sur le bus afin que les methodes "@Subscribe" soient invoquees automatiquement.
        this.process = p;
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

    // Declaration de la methode de callback invoquee lorsqu'un message de type Bidule transite sur le bus
    @Subscribe
    public void onReceive(DedicatedMessage b){
        if (b.getDest() == this.getId()){
            //je met à jour mon horloge de lamport
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
        //je met à jour mon horloge de lamport
        //si on est en request dans la méthode request on attend que la variable soit en sectioncritique, quand on recoit le token on passe en section critique, quand on passe en release on passe au suivant
        //si on est à null, on passe au suivant
        if (b.getDest() == this.getId()){
            if (b.getEstampillage() > this.horloge){
                this.horloge = b.getEstampillage();
            }
            this.horloge++;
            System.out.println(Thread.currentThread().getName() + " receives broadcast: " + b.getMessage() + " for " + this.process.getName());
            System.out.println(Thread.currentThread().getName() + " horloge after receive : " + this.horloge);
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

    }

    public void releaseSC(){

    }
}
