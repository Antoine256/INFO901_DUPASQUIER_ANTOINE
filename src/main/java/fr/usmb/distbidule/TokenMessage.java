package fr.usmb.distbidule;

public class TokenMessage extends Message{
    private final Token token;
    private int dest;

    public TokenMessage(Token token, int dest){
        super(token);
        this.token = token;
    }

    @Override
    public String toString(){
        return ""+this.getMessage();
    }

    public Token getToken(){
        return this.token;
    }

    public int getDest(){
        return this.dest;
    }
}
