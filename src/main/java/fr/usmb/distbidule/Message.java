package fr.usmb.distbidule;

public abstract class Message<T> {
    private int estampillage = 0;
    private T message = null;

    public Message(T message){
        this.message = message;
    }

    public T getMessage(){
        return this.message;
    }

    public int getEstampillage(){
        return this.estampillage;
    }

    public abstract String toString();

    public void setEstampillage(int horloge) {
        this.estampillage = horloge;
    }
}
