package fr.usmb.distbidule;

import java.util.Objects;

public class Process  implements Runnable {
	private Thread thread;
	private boolean alive;
	private boolean dead;
	public static final int maxNbProcess = 3;
	private static int nbProcess = 0;
	private int id = Process.nbProcess++;
	private State tokenState = State.Null;
	private Comm com;

	public Process(String name){
		this.thread = new Thread(this);
		this.thread.setName(name);
		this.alive = true;
		this.dead = false;
		this.thread.start();
		this.com = new Comm(this);
	}

	public void run(){
		int loop = 0;

		System.out.println(Thread.currentThread().getName() + " id :" + this.id);

		while(this.alive){
			System.out.println(Thread.currentThread().getName() + " Loop : " + loop);
			try{
				Thread.sleep(500);
				if (Objects.equals(this.getName(), "P0")){
					this.com.sendTo("j'appelle 2 et je te recontacte après", 1);
				}
				if (Objects.equals(this.getName(), "P1")){
					this.com.broadcast("J'attend un appel");
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			loop++;
		}

		System.out.println(Thread.currentThread().getName() + " stopped");
		this.dead = true;
	}

	public String getName() {
		return this.thread.getName();
	}

	public void waitStoped(){
		while(!this.dead){
			try{
				Thread.sleep(500);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}

	public void stop(){
		this.alive = false;
	}

	public int getId(){
		return this.id;
	}

	public Thread getThread(){
		return this.thread;
	}

}
