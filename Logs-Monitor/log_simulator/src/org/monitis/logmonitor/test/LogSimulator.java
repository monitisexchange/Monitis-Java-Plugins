package org.monitis.logmonitor.test;

import org.apache.log4j.Logger;

public class LogSimulator extends Thread {
    private Logger logger =Logger.getLogger("MONITIS");
    private String[] msg = new String[10];
    
    public LogSimulator(){
    	msg[0]="FINE - no problem yet.";
    	msg[1]="ERROR exception in the parent class.";
    	msg[2]="Warning - you cannot call this method with NULL param.";
    	msg[3]="SERIOUS exceptions occurred.";
    	msg[4]="Still normal processing.";
    	msg[5]="fatal - application will be terminated.";
    	msg[6]="Requests from 127.0.0.1 host.";
    	msg[7]="You could probably leave a script in cron.";
    	msg[8]="Attention - make sure that the script is still running.";
    	msg[9]="Assuming that you have GNU tail.";
    }
    
    @Override
    public void run() {
    	while (true){
    		int i = (int)(Math.random()*9.9);
    		if (i < 4 )	{
    			logger.error(msg[i]);
    		} else
    		if (i < 6) {
    			logger.info(msg[i]);
    		} else {
    			logger.warn(msg[i]);
    		}
    		try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {	}
    	}
    }

	public static void main(String[] args) {
		new LogSimulator().start();

	}

}
