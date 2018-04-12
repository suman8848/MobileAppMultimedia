package com.android.musicplayertemplate;

/**
 * Created by Dinesh on 11/9/2016.
 */

public class MediaUtils {

    public String milliSecontsToTImer(long milliseconds){
        String finalTimerString="";
        String secondsString="";

        int hrs= (int) (milliseconds/(1000*60*60));
        int min= (int) ((milliseconds%(1000*60*60))/(1000*60));
        int sec= (int) ((milliseconds%(1000*60*60))%(1000*60)/1000);
        if(hrs>0){
            finalTimerString=hrs+":";
        }

        if(sec<10){
            secondsString="0"+sec;
        }else{
            secondsString=""+sec;
        }
        finalTimerString=finalTimerString+min+":"+secondsString;

        return  finalTimerString;

    }


    /**
     * Function to get Progress percentage
     * @param currentDuration
     * @param totalDuration
     * */
    public int getProgressPercentage(long currentDuration, long totalDuration){
        Double percentage = (double) 0;

        long currentSeconds = (int) (currentDuration / 1000);
        long totalSeconds = (int) (totalDuration / 1000);

        // calculating percentage
        percentage =(((double)currentSeconds)/totalSeconds)*100;

        // return percentage
        return percentage.intValue();
    }

    /**
     * Function to change progress to timer
     * @param progress -
     * @param totalDuration
     * returns current duration in milliseconds
     * */
    public int progressToTimer(int progress, int totalDuration) {
        int currentDuration = 0;
        totalDuration = (int) (totalDuration / 1000);
        currentDuration = (int) ((((double)progress) / 100) * totalDuration);

        // return current duration in milliseconds
        return currentDuration * 1000;
    }
}
