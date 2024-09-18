package fr.usmb.distbidule;

import java.util.ArrayList;

public class Launcher{

	public static void main(String[] args){

		ArrayList<Process> processes = new ArrayList<Process>();

		for(int i=0; i<Com.maxNbProcess; i++) {
			processes.add(new Process("P"+i));
		}

		try{
			Thread.sleep(2000);
		}catch(Exception e){
			e.printStackTrace();
		}

		for(int i=0; i<Com.maxNbProcess; i++) {
			processes.get(i).stop();
		}

		for(int i=0; i<Com.maxNbProcess; i++) {
			processes.get(i).waitStoped();
		}
	}
}
