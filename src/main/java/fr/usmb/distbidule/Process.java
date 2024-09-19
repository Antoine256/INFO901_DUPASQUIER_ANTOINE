package fr.usmb.distbidule;

import java.util.Objects;

public class Process  implements Runnable {
	private Thread thread;
	private boolean alive;
	private boolean dead;
	private Com com;

	public Process(String name){
		this.com = new Com(this);

		this.thread = new Thread(this);
		this.thread.setName(name);
		this.alive = true;
		this.dead = false;
		this.thread.start();
	}

	public void run(){
		int loop = 0;

		System.out.println(Thread.currentThread().getName() + " id :" + this.com.getId());

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

	public Thread getThread(){
		return this.thread;
	}

	public boolean isAlive(){
		return this.alive;
	}
}
