package bot.slack;

import bot.common.ErrorMessaging;
import bot.schedule.ScheduleEvent;
import bot.schedule.ScheduleException;
import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;

/**
 * Basic code structure for this class created by user Ramswaroop
 * at https://github.com/ramswaroop/jbot/tree/master/jbot
 */
@Component
public class SlackBot extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(SlackBot.class);
    private static ArrayList<ScheduleEvent> events = new ArrayList<>();
    /**
     * Slack token from application.properties file. You can get your slack token
     * next <a href="https://my.slack.com/services/new/bot">creating a new bot</a>.
     */
    @Value("${slackBotToken}")
    private String slackToken;


    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }

    /**
     * Invoked when the bot receives a direct mention (@botname: message)
     * or a direct message.
     *
     * @param session
     * @param event
     */
    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) {
        reply(session, event, new Message("Hello! Type /helpme for more "
                + "information."));
    }

    /**
     * Invoked when "help" is detected in chat.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.MESSAGE, pattern = "(help)")
    public void onReceiveMessage(WebSocketSession session, Event event) {
        reply(session, event, new Message("If you need help, type /helpme"));
    }

    @Controller(events = EventType.MESSAGE, pattern = "(What events do I " +
            "have)")
    public void eventRetrieval(WebSocketSession session, Event event) {
        String requestedTimeFrame = "", scheduleString = "", dateSuffix = "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy " +
                "'at' hh:mm aaa");

        String fullRequest = event.getText();
        int endIndex = findEndIndexOfRequest("What events do I have",
                fullRequest);
        requestedTimeFrame = (fullRequest.substring(endIndex + 1)).trim();
        try {
            ArrayList<ScheduleEvent> requestedEvents = getEvents
                    (requestedTimeFrame);

            for (ScheduleEvent currentEvent : requestedEvents) {
                int currentDayNumber = 0;
                currentDayNumber = ScheduleEvent.getDateFromText(currentEvent
                        .getEventDate())[1];

                dateSuffix = getDayNumberSuffix(currentDayNumber);
                SimpleDateFormat outputFormat = new SimpleDateFormat("EEEEEEEEE, " +
                        "MMMMMMMMM d'" + dateSuffix + "', yyyy 'at' h:mm " +
                        "aaa");
                try {
                    Date date = inputFormat.parse(currentEvent.getEventDate() +
                            " at " + currentEvent.getEventTime());
                    scheduleString += currentEvent.getEventName() + " on " + outputFormat
                            .format(date) + "\n";
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            reply(session, event, new Message("Your events for " +
                    requestedTimeFrame + ":\n\n" + scheduleString));
        } catch (ScheduleException execep) {
            RichMessage accessor = new RichMessage();
            ErrorMessaging.setErrorMessage(execep, accessor);
            reply(session, event, new Message(accessor.getText() + accessor
                    .getAttachments()[0].getText()));
        }
    }

    /**
     * Invoked when an item is pinned in the channel.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.PIN_ADDED)
    public void onPinAdded(WebSocketSession session, Event event) {
        reply(session, event, new Message("Thanks for the pin! You can find all pinned items under channel details."));
    }

    /**
     * Invoked when bot receives an event of type file shared.
     * NOTE: You can't reply to this event as slack doesn't send
     * a channel id for this event type. You can learn more about
     * <a href="https://api.slack.com/events/file_shared">file_shared</a>
     * event from Slack's Api documentation.
     *
     * @param session
     * @param event
     */
    @Controller(events = EventType.FILE_SHARED)
    public void onFileShared(WebSocketSession session, Event event) {
        logger.info("File shared: {}", event);
    }


    /**
     * Conversation feature of JBot. This method is the starting point of the conversation (as it
     * calls {@link Bot#startConversation(Event, String)} within it. You can chain methods which will be invoked
     * one after the other leading to a conversation. You can chain methods with {@link Controller#next()} by
     * specifying the method name to chain with.
     *
     * @param session
     * @param event
     */
    @Controller(pattern = "(\\bbutt\\b|\\bidiot\\b||\\bpiss\\b)", next = "apology")
    public void shameSwearing(WebSocketSession session, Event event) {
        startConversation(event, "apology");
        reply(session, event, new Message("Why would you use that word, are you sorry?"));
    }

    /**
     * This method is chained with {@link SlackBot#shameSwearing(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    @Controller
    public void apology(WebSocketSession session, Event event) {
        if(event.getText().equalsIgnoreCase("yes")){
            reply(session, event, new Message("Good to hear, now be good"));
        }else{
            reply(session, event, new Message("You're bad and should feel bad for discovering that I have not power over you"));
        }
        stopConversation(event);    // jump to next question in conversation
    }

    public static String getDayNumberSuffix(int day) {
        if (day >= 11 && day <= 13) {
            return "th";
        }
        switch (day % 10) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public static void addScheduleEvent(ScheduleEvent event) {
        events.add(event);
        Collections.sort(events);
    }

    // helper method for populating dates for a week request time frame
    public static ArrayList<String> populateDayRange(String startDateString,
                                                     String endDateString) {
        ArrayList<String> rangeDates = new ArrayList<>();
        SimpleDateFormat inputFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date startDate = null, endDate = null;
        try {
            startDate = inputFormat.parse(startDateString);
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        try {
            endDate = inputFormat.parse(endDateString);
        } catch (ParseException exec) {
            exec.printStackTrace();
        }
        Calendar calendar = Calendar.getInstance();
        int[] startDateArray = ScheduleEvent.getDateFromText(startDateString);
        calendar.set(startDateArray[2], startDateArray[0]-1, startDateArray[1]);
        do {
            rangeDates.add(inputFormat.format(startDate));
            calendar.add(Calendar.DATE, 1);
            try {
                if (!startDate.after(endDate)) {
                    startDate = inputFormat.parse((calendar.get(Calendar.MONTH) + 1) +
                            "/" + calendar.get(Calendar.DATE) + "/" + calendar.get
                            (Calendar.YEAR));
                }
            } catch (ParseException exec) {
                exec.printStackTrace();
            }
        } while (!startDate.after(endDate));
        return rangeDates;
    }

    public static ArrayList<ScheduleEvent> getEventList() {
        return events;
    }


    public static ArrayList<ScheduleEvent> getEvents(String requestedTimeFrame)
            throws ScheduleException {
        ArrayList<ScheduleEvent> requestedEvents = new ArrayList<>();
        ArrayList<String> dayInputs = new ArrayList<>();
        ArrayList<String> rangeDates;
        setupDayInputs(dayInputs);
        boolean isDateFormat = false;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat numberedDateFormat = new SimpleDateFormat
                ("MM/dd/yyyy");
        Date requestedDate = null;
        Date currentDate = null;
        try {
            currentDate = numberedDateFormat.parse((calendar.get
                    (Calendar.MONTH) + 1) + "/"
                    + calendar.get(Calendar.DATE) + "/" + calendar
                    .get(Calendar.YEAR));
        } catch (ParseException execept) {
            execept.printStackTrace();
        }
        String requestedTimeLower = requestedTimeFrame.toLowerCase();
        //check if it a specified date of format mm/dd/yyyy
        try {
            requestedDate = numberedDateFormat.parse(requestedTimeFrame);
            isDateFormat = true;
        } catch (ParseException exec) {
            int requestedDayDifference = 0;
            if (ScheduleEvent.daysOfWeek.containsValue(requestedTimeLower) ||
                    requestedTimeLower.equals("tomorrow") ||
                    requestedTimeLower.equals("today")) {
                requestedDayDifference = ScheduleEvent.findDayDifference
                        (requestedTimeLower);
            } else if (requestedTimeLower.equals("next week")) {
                requestedDayDifference = ScheduleEvent.findDayDifference
                        (ScheduleEvent.daysOfWeek.get(calendar
                                .getFirstDayOfWeek()));
            }

            /*
             * assures that non-numerical date strings are one of the below
             * choices
             */
            switch (requestedTimeLower) {
                case "this week":
                    break;
                case "this month":
                    break;
                case "next month":
                    calendar.add(Calendar.MONTH, 1);
                    break;
                case "this year":
                    break;
                case "next year":
                    calendar.add(Calendar.YEAR, 1);
                    break;
                case "next week":
                case "today":
                case "tomorrow":
                case "sunday":
                case "monday":
                case "tuesday":
                case "wednesday":
                case "thursday":
                case "friday":
                case "saturday":
                    calendar.add(Calendar.DATE, requestedDayDifference);
                    break;
                default:
                    throw new ScheduleException("DateFormat");
            }
        }
        try {
            //if requested time frame is a single day, determine its date
            if (dayInputs.contains(requestedTimeLower)) {
                requestedDate = numberedDateFormat.parse
                        ((calendar.get(Calendar.MONTH) + 1) + "/" + calendar
                                .get(Calendar.DATE) + "/" + calendar.get
                                (Calendar.YEAR));
            }
            calendar = Calendar.getInstance();
            /*
             * for loop that adds all events that match the request criteria
             * to an arraylist
             */
            for (ScheduleEvent currentEvent : events) {
                //date of currently found event in list of events
                Date currentLoopEventDate = numberedDateFormat.parse
                        (currentEvent.getEventDate());
               /*
                *  if the requested time frame is a range rather than a single
                *  day
                */
                if (!dayInputs.contains(requestedTimeLower) && isDateFormat ==
                        false) {
                    Calendar rangeCalendar = Calendar.getInstance();

                    /*
                     * if range was a week, this loops through the dates in
                     * requested week range and adds the  current event to
                     * the requested list if it is within that requested range
                      * of dates.
                     */
                    SimpleDateFormat inputFormat = new SimpleDateFormat
                            ("MM/dd/yyyy");
                    if (requestedTimeLower.equals("this week") ||
                            requestedTimeLower.equals("next week")) {
                        String beginDate = "", endDate = "";
                        int dateDifference;
                        if (requestedTimeFrame.equals("this week")) {
                            beginDate = (rangeCalendar.get(Calendar.MONTH) + 1) +
                                    "/" + rangeCalendar.get(Calendar.DATE) + "/"
                                    + rangeCalendar.get(Calendar.YEAR);
                            if (rangeCalendar.get(Calendar.DAY_OF_WEEK) !=
                                    Calendar.SATURDAY) {
                                dateDifference = ScheduleEvent
                                        .findDayDifference("saturday");
                                rangeCalendar.add(Calendar.DATE, dateDifference);
                            }


                        } else {
                            dateDifference = ScheduleEvent
                                    .findDayDifference("sunday");
                            rangeCalendar.add(Calendar.DATE, dateDifference);
                            beginDate = (rangeCalendar.get(Calendar.MONTH) + 1)
                                    + "/" + rangeCalendar.get(Calendar.DATE) +
                                    "/" + rangeCalendar.get(Calendar.YEAR);
                            rangeCalendar.add(Calendar.DATE, 6);

                        }
                        endDate = (rangeCalendar.get(Calendar.MONTH) + 1) + "/"
                                + rangeCalendar.get(Calendar.DATE) + "/"
                                + rangeCalendar.get(Calendar.YEAR);
                        rangeDates = populateDayRange(beginDate, endDate);
                        /*
                         * loops through dates in the requested range, checks if
                         * any of those dates matches the current event's date,
                         * and if one matches the event date, adds that event
                         * to the list
                        */
                        for (String currentRangeDateString : rangeDates) {
                            Date rangeDate = numberedDateFormat.parse
                                    (currentRangeDateString);
                            if (rangeDate.equals(currentLoopEventDate)) {
                                requestedEvents.add(currentEvent);
                            }
                        }
                    } else {
                        rangeCalendar = Calendar.getInstance();
                        if (requestedTimeLower.equals("this month") ||
                                requestedTimeLower.equals("next month")) {
                            int currentMonthNumber =
                                    rangeCalendar.get(Calendar.MONTH)
                                            + 1;
                            int nextMonthNumber = currentMonthNumber + 1;
                            /*
                             * if request is "this month" and the current
                             * event's month is equal to the current month
                             * number, or request is "next month" and the
                             * current event's month is equal to the next
                             * month's number
                             */
                            if (requestedTimeLower.equals("this month") &&
                                    ScheduleEvent.getDateFromText(currentEvent
                                            .getEventDate())[0] == currentMonthNumber
                                    || requestedTimeLower.equals("next month")
                                    && ScheduleEvent.getDateFromText
                                    (currentEvent.getEventDate())[0]
                                    == nextMonthNumber) {
                                requestedEvents.add(currentEvent);
                            }
                        } else {
                            int currentYearNumber = rangeCalendar.get(Calendar
                                    .YEAR);
                            int nextYearNumber = currentYearNumber + 1;
                             /*
                             * if request is "this year" and the current
                             * event's year is equal to the current year
                             * number, or request is "next year" and the
                             * current event's year is equal to the next
                             * year's number
                             */
                            if (requestedTimeFrame.equals("this year") &&
                                    ScheduleEvent.getDateFromText(currentEvent
                                            .getEventDate())[2] == currentYearNumber
                                    || requestedTimeFrame.equals("next" +
                                    " year") && ScheduleEvent.getDateFromText(currentEvent
                                    .getEventDate())[2] == nextYearNumber) {
                                requestedEvents.add(currentEvent);
                            }
                        }

                    }
                } else {
                    //assures date requested was not a date from the past
                    if (requestedDate.before(currentDate)) {
                        throw new ScheduleException("PastDate");
                    }
                    //if the current event matches the requested event date
                    if (requestedDate.equals(currentLoopEventDate)) {
                        requestedEvents.add(currentEvent);
                    }
                }
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return requestedEvents;
    }

    /*
     * helper method used to find the end of the request format and find
     * the beginning of create a substring of the requested time frame
     */
    private int findEndIndexOfRequest(String requestString, String
            fullRequest) {
        return fullRequest.indexOf(requestString) + 20;
    }

    //helper method that assigns a list of individual day strings
    public static void setupDayInputs(ArrayList<String> dayInputs) {
        dayInputs.add("today");
        dayInputs.add("tomorrow");
        dayInputs.add("sunday");
        dayInputs.add("monday");
        dayInputs.add("tuesday");
        dayInputs.add("wednesday");
        dayInputs.add("thursday");
        dayInputs.add("friday");
        dayInputs.add("saturday");
    }
    public static void resetEventList() {
        events = new ArrayList<>();
    }



    /**
     * Conversation feature of JBot. This method is the starting point of the conversation (as it
     * calls {@link Bot#startConversation(Event, String)} within it. You can chain methods which will be invoked
     * one after the other leading to a conversation. You can chain methods with {@link Controller#next()} by
     * specifying the method name to chain with.
     *
     * @param session
     * @param event
     */
    /**
     * @Controller(pattern = "(setup meeting)", next = "confirmTiming")
     * public void setupMeeting(WebSocketSession session, Event event) {
     * startConversation(event, "confirmTiming");   // start conversation
     * reply(session, event, new Message("Cool! At what time (ex. 15:30) do you
     * want me to set up the meeting?"));
     * }
     */


    /**
     * This method is chained with {@link SlackBot#setupMeeting(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    /**
     * @Controller(next = "askTimeForMeeting")
     * public void confirmTiming(WebSocketSession session, Event event) {
     *   reply(session, event, new Message("Your meeting is set at " + event
     *   .getText() +
     *   ". Would you like to repeat it tomorrow?"));
     *   nextConversation(event);    // jump to next question in conversation
     * }
     */


    /**
     * This method is chained with {@link SlackBot#confirmTiming(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    /**
     * @Controller(next = "askWhetherToRepeat")
     * public void askTimeForMeeting(WebSocketSession session, Event event) {
     *   if (event.getText().contains("yes")) {
     *       reply(session, event, new Message("Okay. Would you like me to
     *       set a reminder for you?"));
     *       nextConversation(event);    // jump to next question in
     *       conversation
     *   } else {
     *      reply(session, event, new Message("No problem. You can always
     *      schedule one with 'setup meeting' command."));
     *      stopConversation(event);    // stop conversation only if user
     *       says no
     *   }
     * }
     */


    /**
     * This method is chained with {@link SlackBot#askTimeForMeeting(WebSocketSession, Event)}.
     *
     * @param session
     * @param event
     */
    /**
     * @Controller
     * public void askWhetherToRepeat(WebSocketSession session, Event event) {
     * public void askWhetherToRepeat(WebSocketSession session, Event event) {
     *    if (event.getText().contains("yes")) {
     *        reply(session, event, new Message("Great! I will remind you
     *         tomorrow before the meeting."));
     *    } else {
     *        reply(session, event, new Message("Oh! my boss is smart enough
     *         to remind himself :)"));
     *    }
     *    stopConversation(event);    // stop conversation
     * }
     */
}
