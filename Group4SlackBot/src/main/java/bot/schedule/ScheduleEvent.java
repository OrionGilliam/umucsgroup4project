package bot.schedule;

import java.util.Calendar;
import java.util.*;
import java.util.Map;

public class ScheduleEvent {
    String eventName;
    public ScheduleEvent(String scheduleText) {
        HashMap<Integer, String> daysOfWeek = new HashMap<Integer, String>();
        setDayMap(daysOfWeek);
        Scanner textScanner = new Scanner(scheduleText);
        try {

            eventName = textScanner.findInLine("\".*\"");
            System.out.println(eventName);

          /*  String date = textScanner.next();
            if (date.) {

            }
            String time = textScanner.next();*/
        }catch (NoSuchElementException exec){

        }

    }
    private void setDayMap (HashMap<Integer, String>dayMap) {
        dayMap.put(Calendar.SUNDAY, "sunday");
        dayMap.put(Calendar.MONDAY, "monday");
        dayMap.put(Calendar.TUESDAY, "tuesday");
        dayMap.put(Calendar.WEDNESDAY, "wednesday");
        dayMap.put(Calendar.THURSDAY, "thursday");
        dayMap.put(Calendar.FRIDAY, "friday");
        dayMap.put(Calendar.SATURDAY, "saturday");
    }


}