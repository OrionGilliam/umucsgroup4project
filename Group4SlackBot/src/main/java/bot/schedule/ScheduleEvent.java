package bot.schedule;

import bot.slack.SlackBot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleEvent implements Comparable<ScheduleEvent> {
    public static HashMap<Integer, String> daysOfWeek;
    private static Calendar calendar;
    private String eventName, eventDate, eventTime;

    public ScheduleEvent(String scheduleText) throws ScheduleException {
        calendar = Calendar.getInstance();
        daysOfWeek = new HashMap<>();
        setDayMap(daysOfWeek);
        Scanner textScanner = new Scanner(scheduleText);
        String nextString, concatString = "";
        try {
            //if the event name properly starts with a double quote
            if ((nextString = textScanner.next()).charAt(0) == '\"') {
                concatString += nextString;
                //this outer do-while scans for and formats the event name
                do {
                    /*
                     * if the end of the currently scanned string end in a
                     * double quote
                     */
                    if (concatString.charAt(concatString.length() - 1) == '\"') {
                        //if the event name is empty
                        if(concatString.length() == 2 && countDoubleQuotes
                                (concatString) == 2){
                            throw new ScheduleException("EventFormat");
                        }
                        eventName = concatString;
                        //removes excess spaces from event name
                        eventName = eventName.replaceAll("\\s+", " ");
                        //removes spaces at beginning and end of string
                        eventName = eventName.trim();
                        /**
                         * this loop ensures the end double quote of the event is not in
                         * the middle of other characters
                         */
                        verifyDate(textScanner);
                        verifyTime(textScanner);
                        break;
                    } else if (countDoubleQuotes(concatString) != 1) {
                        throw new ScheduleException("EventFormat");
                    }
                    if (!textScanner.hasNext()) {
                        if (countDoubleQuotes(concatString) == 1) {
                            throw new ScheduleException("EventFormat");
                        }
                        break;
                    }
                    nextString = textScanner.next();
                    concatString += " " + nextString;
                } while (true);
            } else {
                throw new ScheduleException("EventFormat");
            }
        } catch (NoSuchElementException exec) {

        }

    }

    /*
     * gets the Calendar value of given day of the week from the daysOfWeek
     * hashmap
     */
    public static int getDayValue(String dateText) {
        for (HashMap.Entry<Integer, String> entry : daysOfWeek.entrySet()) {
            dateText = dateText.toLowerCase();
            if (entry.getValue().equals(dateText)) {
                return entry.getKey().intValue();
            }
        }
        return -1;
    }

    /*
     * helper method that gets the date(or day of month) difference between the
     * current day of the week and the given day of the week
     */
    public static int findDayDifference(String dateText) {
        Calendar referenceCalendar = Calendar.getInstance();
        dateText = dateText.toLowerCase();
        int dateDifference = 0;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try  {
            referenceCalendar.setTime(dateFormat.parse(dateText));
            dateText = daysOfWeek.get(referenceCalendar.get(Calendar
                    .DAY_OF_WEEK));
            referenceCalendar = Calendar.getInstance();
        } catch (ParseException exec) {

        }

        if (dateText.equals("today")) {
            return 0;
        }
        if(dateText.equals("tomorrow")){
            return 1;
        }
        int dayValue = getDayValue(dateText);

        //if event date is later in the week than current day
        if (getDayValue(dateText) > referenceCalendar.get(Calendar.DAY_OF_WEEK)) {
            dateDifference = dayValue - referenceCalendar.get(Calendar.DAY_OF_WEEK);
        } else {
            dateDifference = (7 - referenceCalendar.get(Calendar.DAY_OF_WEEK)) +
                    dayValue;
        }
        return dateDifference;

    }

    //resets the calendar used throughout this class' methods
    private static void resetCalendar() {
        calendar = Calendar.getInstance();
    }

    /*
     * retrieves date value from string of mm/dd/yyyy format instead of using
     * deprecated Date object methods
     */
    public static int[] getDateFromText(String dateString) {
        Scanner dateScanner = new Scanner(dateString);
        int[] dateParts = new int[3];
        dateScanner.useDelimiter("/");
        for (int index = 0; index < 3; index++) {
            dateParts[index] = Integer.parseInt(dateScanner.next());
        }
        return dateParts;
    }

    /*
     * initializes a hashmap that associates the Calendar days' values with
     * their names
     */
    private void setDayMap(HashMap<Integer, String> dayMap) {
        dayMap.put(Calendar.SUNDAY, "sunday");
        dayMap.put(Calendar.MONDAY, "monday");
        dayMap.put(Calendar.TUESDAY, "tuesday");
        dayMap.put(Calendar.WEDNESDAY, "wednesday");
        dayMap.put(Calendar.THURSDAY, "thursday");
        dayMap.put(Calendar.FRIDAY, "friday");
        dayMap.put(Calendar.SATURDAY, "saturday");
    }

    /*
     * helper method that is used to check how many double quotes are
     * currently in the scanned event name string
     */
    private int countDoubleQuotes(String givenString) {
        int quoteCount = 0;
        for (int currentCharIndex = 0; currentCharIndex < givenString.length()
                ; currentCharIndex++) {
            if (givenString.charAt(currentCharIndex) == '"') {
                quoteCount += 1;
            }
        }
        return quoteCount;
    }

    /*
     * verifies that the date input is correct and sets the ScheduleEvent to
     * that input
     */
    private void verifyDate(Scanner textScanner) throws ScheduleException {
        String dateText;
        SimpleDateFormat dateFormat = new SimpleDateFormat
                ("MM/dd/yyyy");
        try {
            int dateDifference = 0;
            dateText = textScanner.next();
            resetCalendar();
            //if date entered is a day of the week, "today", or "tomorrow"
            if (daysOfWeek.containsValue(dateText.toLowerCase()) ||
                    dateText.toLowerCase().equals("today") || dateText
                    .toLowerCase().equals("tomorrow")) {
                dateDifference = findDayDifference(dateText);
                if (daysOfWeek.containsValue(dateText.toLowerCase()) || dateText
                        .toLowerCase().equals("tomorrow")) {
                    calendar.add(Calendar.DATE, dateDifference);
                }
                eventDate = ((calendar.get(Calendar.MONTH) + 1) + "/" +
                        calendar.get(Calendar.DAY_OF_MONTH)
                        + "/" + calendar.get(Calendar.YEAR));
                resetCalendar();
            } else {
                resetCalendar();
                Date enteredDate = dateFormat.parse(dateText);
                Date currentDate = dateFormat.parse((calendar.get
                        (Calendar.MONTH) + 1) + "/" + calendar
                        .get(Calendar.DAY_OF_MONTH) + "/" + calendar
                        .get(Calendar.YEAR));
                resetCalendar();
                if (enteredDate.after(currentDate)) {
                    eventDate = dateFormat.format(enteredDate);
                } else {
                    throw new ScheduleException("PastDate");
                }
            }
        } catch (NoSuchElementException | ParseException exec) {
            throw new ScheduleException("DateFormat");
        }
    }

    /*
     * verifies that the time input is correct and sets the ScheduleEvent to
     * that input
     */
    private void verifyTime(Scanner textScanner) throws ScheduleException {
        String timeText;
        try {
            timeText = textScanner.next() + " " + textScanner.next();
            SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aaa");
            Date timeHolder = timeFormat.parse(timeText);
            eventTime = timeFormat.format(timeHolder);
        } catch (NoSuchElementException | ParseException exec) {
            throw new ScheduleException("TimeFormat");
        }

    }

    public String getEventDate() {
        return eventDate;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventTime() {
        return eventTime;
    }

    @Override
    public String toString() {
        SimpleDateFormat inputFormat = new SimpleDateFormat
                ("MM/dd/yyyy " +
                        "'at' hh:mm aaa");
        String dateSuffix = SlackBot.getDayNumberSuffix
                (getDateFromText(eventDate)[1]);

        Date date = null;
        try {
            date = inputFormat.parse(eventDate +
                    " at " + eventTime);
        } catch (ParseException exec) {
            exec.printStackTrace();
        }
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEEEEEEEE, " +
                "MMMMMMMMM d'" + dateSuffix + "', yyyy 'at' h:mm " +
                "aaa");
        return eventName + " on " + outputFormat.format(date);
    }

    @Override
    public int compareTo(ScheduleEvent secondEvent) {
        int[] firstDateArray = getDateFromText(eventDate);
        int[] secondDateArray = getDateFromText(secondEvent.eventDate);
        int[] firstTimeArray = new int[2];
        int[] secondTimeArray = new int[2];
        String firstTimeSuffix, secondTimeSuffix;
        Scanner timeScanner = new Scanner(eventTime);
        timeScanner.useDelimiter("(\\s|:)");
        firstTimeArray[0] = new Integer(timeScanner.next());
        firstTimeArray[1] = new Integer(timeScanner.next());
        firstTimeSuffix = timeScanner.next();
        timeScanner = new Scanner(secondEvent.eventTime);
        timeScanner.useDelimiter("(\\s|:)");
        secondTimeArray[0] = new Integer(timeScanner.next());
        secondTimeArray[1] = new Integer(timeScanner.next());
        secondTimeSuffix = timeScanner.next();
        String firstEventName = eventName, secondEventName = secondEvent
                .eventName;
        Integer firstValue, secondValue;
        //compares year
        if ((firstValue = new Integer(firstDateArray[2])).compareTo(secondValue =
                new Integer(secondDateArray[2])) != 0) {
            return firstValue.compareTo(secondValue);
        }
        //compares month
        if ((firstValue = new Integer(firstDateArray[0])).compareTo
                (secondValue = new Integer(secondDateArray[0])) != 0) {
            return firstValue.compareTo(secondValue);
        }
        //compares day of month (date)
        if ((firstValue = new Integer(firstDateArray[1])).compareTo
                (secondValue = new Integer(secondDateArray[1])) != 0) {
            return firstValue.compareTo(secondValue);
        }
        //compares time suffixes (AM and PM)
        if (firstTimeSuffix.toLowerCase().compareTo(secondTimeSuffix
                .toLowerCase()) != 0) {
            return firstTimeSuffix.toLowerCase().compareTo(secondTimeSuffix
                    .toLowerCase());
        }
        //compares hour
        if ((firstValue = new Integer(firstTimeArray[0])).compareTo
                (secondValue = new Integer(secondTimeArray[0])) != 0) {
            return firstValue.compareTo(secondValue);
        }
        //compares minutes
        if ((firstValue = new Integer(firstTimeArray[1])).compareTo
                (secondValue = new Integer(secondTimeArray[1])) != 0) {
            return firstValue.compareTo(secondValue);
        }
        //compares names
        if ((firstEventName.compareTo(secondEventName)) != 0) {
            return firstEventName.compareTo(secondEventName);
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {

        if (!(object instanceof ScheduleEvent)) {
            return false;
        }
        ScheduleEvent secondEvent = (ScheduleEvent) object;
        return this.compareTo(secondEvent) == 0;

    }
}