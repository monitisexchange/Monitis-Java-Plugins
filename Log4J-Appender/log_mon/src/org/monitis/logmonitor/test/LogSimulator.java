package org.monitis.logmonitor.test;

import org.apache.log4j.Logger;

public class LogSimulator extends Thread {
    private Logger logger =Logger.getLogger(this.getClass().getSimpleName());//("MONITIS");
    private String[] msg = new String[10];
    
    public LogSimulator(){
    	msg[0]="SUPER no problem";
    	msg[1]="ERROR kuku blin";
    	msg[2]="Warning kak blin";
    	msg[3]="SERIOUS exceptions";
    	msg[4]="Normal processing";
    	msg[5]="fatal 564jhgjhagdkjah";
    	msg[6]="AAAAAAAAAAkjlkjl877879 lkkj";
    	msg[7]="You could probably leave a script in cron";
    	msg[8]="Attention - make sure that the script is still running";
    	msg[9]="Assuming that you have GNU tail";
    }
    
    @Override
    public void run() {
    	while (true){
    		int i = (int)(Math.random()*9.9);
    		if (i < 3 )	{
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
