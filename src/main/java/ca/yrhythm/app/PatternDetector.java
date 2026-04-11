package ca.yrhythm.app;

import java.util.List;

public class PatternDetector {

    public boolean downwardTrend(List<JournalEntry> entries){

        if(entries.size() < 5) return false;

        int decline = 0;

        for(int i=1;i<entries.size();i++){

            int prev = entries.get(i-1).getMood().getIntensity();
            int curr = entries.get(i).getMood().getIntensity();

            if(curr < prev){
                decline++;
            }
        }

        return decline >= entries.size()/2;
    }
}
