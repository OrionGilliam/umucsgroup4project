import bot.datetime.DateTimeChecker;
import bot.dieroller.DieRoller;
import bot.help.HelpMe;
import bot.quiz.QuizManager;
import bot.schedule.ScheduleEvent;
import bot.schedule.ScheduleException;
import bot.slack.SlackBot;
import bot.slack.SlashSchedule;
import bot.slack.SlashWiki;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class slashCommandTests {

    Properties props;

    @Before
    public void loadProps() {


    }

    @Test
    public void dieRollerTest() {
        Assert.assertNotEquals(DieRoller.rollByString("3d6"), "Dice format error");
        Assert.assertNotEquals(DieRoller.rollByString("38d69"), "Dice format error");
        Assert.assertNotEquals(DieRoller.rollByString("12D8"), "Dice format error");
        Assert.assertEquals(DieRoller.rollByString("howdy"), "Dice format error");
        Assert.assertEquals(DieRoller.rollByString("b6d8"), "Dice format error");
    }

    @Test
    public void quizTest() {
        int numbQuestions = 2;
        String legitUser = "Boberito";
        String legitUser2 = "Ted";

        JSONObject questions = new JSONObject("{\n" +
                "  \"1\":{\n" +
                "    \"question\": \"What is the capital of California?\",\n" +
                "    \"a\": \"Los Angeles\",\n" +
                "    \"b\": \"Sacramento\",\n" +
                "    \"c\": \"Gilroy\",\n" +
                "    \"d\": \"San Francisco\",\n" +
                "    \"answer\":\"b\"\n" +
                "  },\n" +
                "\n" +
                "  \"2\":{\n" +
                "    \"question\": \"Who scored the greated number of NHL goals?\",\n" +
                "    \"a\": \"Brett Hull\",\n" +
                "    \"b\": \"Jari Kurri\",\n" +
                "    \"c\": \"Wayne Gretzky\",\n" +
                "    \"d\": \"Keither Tkachuk\",\n" +
                "    \"answer\": \"c\"\n" +
                "  },\n" +
                "\n" +
                "  \"3\":{\n" +
                "    \"question\": \"When founded in 1889, Nintendo was a producer of what product?\",\n" +
                "    \"a\": \"Playing cards\",\n" +
                "    \"b\": \"Board games\",\n" +
                "    \"c\": \"Soap\",\n" +
                "    \"d\": \"Swords\",\n" +
                "    \"answer\": \"a\"\n" +
                "  }\n" +
                "\n" +
                "}");

        QuizManager qm = new QuizManager(numbQuestions, questions);

        //verifies bad information is given error return
        Assert.assertEquals(qm.conductQuiz(legitUser, "boo loo"), "You have not entered a valid command for the quiz, please try 'trivia'");

        //verifies that two questions are asked and that the user is thanked at the end.  Done with concurrent users
        qm.conductQuiz(legitUser, "trivia");
        qm.conductQuiz(legitUser2, "trivia");
        qm.conductQuiz(legitUser, "a");
        qm.conductQuiz(legitUser2, "a");
        Assert.assertTrue(qm.conductQuiz(legitUser, "b").contains("Thank you"));
        Assert.assertTrue(qm.conductQuiz(legitUser2, "b").contains("Thank you"));
    }

    @Test
    public void wikiTest() {
        RichMessage resultHolder;
        SlashWiki slashWiki = new SlashWiki();
        Assert.assertEquals(slashWiki.getWikiResults("Bsadas").getText(),
                "I'm sorry! I could not find a wikipedia page with a title " +
                        "of \"Bsadas\".");

        Assert.assertEquals("Barack obama", (resultHolder = slashWiki.getWikiResults("barack " +
                "obama")).getText());
        Assert.assertEquals("From a " +
                "miscapitalisation: " +
                "This is a redirect from a miscapitalisation. The correct form is given " +
                "by the target of the redirect.", resultHolder.getAttachments
                ()[0].getText());
        resultHolder = null;
        Assert.assertEquals("Barack Obama", (resultHolder = slashWiki.getWikiResults("barack " +
                "Obama")).getText
                ());
        Assert.assertEquals("Barack " +
                "Hussein Obama II (US:  bə-RAHK hoo-SAYN oh-BAH-mə; born August 4, " +
                "1961) is an American politician who served as the 44th President " +
                "of the United States from 2009 to 2017. He is the first African American " +
                "to have served as president.", resultHolder.getAttachments()[0].getText());
        Assert.assertEquals("https://en.wikipedia.org/wiki/Barack_Obama", resultHolder.getAttachments()[1].getText());
        Assert.assertEquals("Error! Please " +
                "enter something to query " +
                "wikipedia for.", slashWiki.getWikiResults("").getText());
        Assert.assertEquals("Error! " +
                "Please enter something to query " +
                "wikipedia for.", slashWiki.getWikiResults("    ").getText());
    }

    @Test
    public void testDateTime() {
        RichMessage mess = DateTimeChecker.checkZone("est");
        Assert.assertTrue(mess.getText().contains("It is"));

        mess = DateTimeChecker.checkZone("blaruug");
        Assert.assertTrue(mess.getText().contains("Please use the format"));
    }

    @Test
    public void testHelpCommand() {
        String testHelp = HelpMe.helpMessageUserGreet("Jeff");
        String correctResponse = "Hello Jeff."
                + " Below is a list of available commands:"

                + "\n/roll [xdy] - Simulates rolling x number of y capacity "
                + "dice (eg. \"/roll 3d6\" rolls 3, 6 sided dice)"

                + "\n/greet - For waking and testing the bot"

                + "\n/wiki [search] - Retrieves the search from Wikipedia (eg. "
                + "\"/wiki dogs\" returns the search for dogs)"

                + "\n/quiz - Starts a trivia game"

                + "\n/datetime [zone] - Retrieves the time for the given 3 "
                + "letter time zone (eg. \"/datetime EST\" returns the date and "
                + "time for Eastern Standard Time)";

        Assert.assertTrue(testHelp.equals(correctResponse));
    }

    @Test
    public void testScheduleCommand() {
        RichMessage richMessage = null;
        String reformattedDateText;
        SlashSchedule slashSchedule = new SlashSchedule();
        String eventName = "";
        String date = "";
        String time = "";
        String errorHead = "";
        String errorAttachment = "";


        Calendar calendar = Calendar.getInstance();

        for (int dayCases = 0; dayCases < 9; dayCases++) {
            switch (dayCases) {
                case 0:
                    eventName = "\"eventName\" ";
                    date = "Sunday";
                    time = " 3:25 AM";
                    break;
                case 1:
                    eventName = "\"new event\" ";
                    date = "Monday";
                    time = " 7:48 PM";
                    break;
                case 2:
                    eventName = "\"Business meeting\" ";
                    date = "Tuesday";
                    time = " 9:25 AM";
                    break;
                case 3:
                    eventName = "\"5k Run\" ";
                    date = "Wednesday";
                    time = " 4:40 PM";
                    break;
                case 4:
                    eventName = "\"Coffee at Starbucks\" ";
                    date = "Thursday";
                    time = " 12:04 PM";
                    break;
                case 5:
                    eventName = "\"Homework due\" ";
                    date = "Friday";
                    time = " 8:52 AM";
                    break;
                case 6:
                    eventName = "\"Gym\" ";
                    date = "Saturday";
                    time = " 4:27 PM";
                    break;
                case 7:
                    eventName = "\"Studying\" ";
                    date = "Today";
                    time = " 11:59 PM";
                    break;
                case 8:
                    eventName = "\"Partying\" ";
                    date = "Tomorrow";
                    time = " 9:30 PM";
                    break;
            }
            richMessage = slashSchedule.getScheduleResults(eventName + date +
                    time);
            reformattedDateText = getDateDynamically(date, calendar, true);
            Assert.assertEquals("Your event has been " +
                    "scheduled: " + eventName + "on " + reformattedDateText + " " +
                    "at" + time, richMessage.getText());
            calendar = Calendar.getInstance();
        }
        SlackBot.resetEventList();

        for (int dateErrorCase = 0; dateErrorCase < 10; dateErrorCase++) {
            String eventString = "";
            String errorMessage = "";
            switch (dateErrorCase) {
                case 0:
                    eventString = "\"\" " +
                            "Wednesday 9:30 PM";
                    errorMessage = "name";
                case 1:
                    eventString = " " +
                            "Wednesday 9:30 PM";
                    errorMessage = "name";
                case 2:
                    eventString = "Walking\" " +
                            "Wednesday 9:30 PM";
                    errorMessage = "name";
                case 3:
                    eventString = "Walking " +
                            "Wednesday 9:30 PM";
                    errorMessage = "name";
                case 4:
                    eventString = "\"Walking " +
                            "Wednesday 9:30 PM";
                    errorMessage = "name";
                case 5:
                    eventString = "\"Walking\" " +
                            "bla 9:30 PM";
                    errorMessage = "date";
                    break;
                case 6:
                    eventString = "\"Walking\" " +
                            "9:52 PM";
                    errorMessage = "date";
                    break;
                case 7:
                    eventString = "\"Walking\" " +
                            "Today 9 PM";
                    errorMessage = "time";
                    break;
                case 8:
                    eventString = "\"Walking\" " +
                            "Today 9:52";
                    errorMessage = "time";
                    break;
                case 9:
                    eventString = "\"Walking\" " +
                            "Wednesday 9:52 dm";
                    errorMessage = "time";
                    break;

            }
            if (errorMessage.equals("name")) {
                errorHead = "Error! Incorrect event " +
                        "name format!";
                errorAttachment = "The event name must be within quotes. Spaces " +
                        "must also be put between event name, " +
                        "date and time.";
            } else if (errorMessage.equals("date")) {
                errorHead = "Error! Incorrect date format!";
                errorAttachment = "The " +
                        "event date must be either a day name " +
                        "(Sunday - Saturday), today, tomorrow, this week, " +
                        "next week, this month, next month, this " +
                        "year, next year, or a specific date in the " +
                        "format mm/dd/yyyy. (ex. 04/15/2019). Spaces must " +
                        "also be put between event name, date and " +
                        "time.";
            } else if (errorMessage.equals("time")) {
                errorHead = "Error! Incorrect time format!";
                errorAttachment = "The event time must be in the form of " +
                        "\"hh:mm aaa\" where h is the hour(number), m is the " +
                        "minute(number) and aaa m is either AM or PM (ex. " +
                        "8:42 AM). Spaces must also be put between event name, " +
                        "date and time.";
            }
            richMessage = slashSchedule.getScheduleResults(eventString);
            Assert.assertEquals(errorHead, richMessage.getText());
            Assert.assertEquals(errorAttachment, richMessage.getAttachments()
                    [0].getText());

        }
        SlackBot.resetEventList();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm aaa");
        for (int pastDateCases = 0; pastDateCases < 6; pastDateCases++) {
            switch (pastDateCases) {
                case 0:
                    calendar.add(Calendar.HOUR, -3);
                    break;
                case 1:
                    calendar.add(Calendar.MINUTE, -20);
                    break;
                case 2:
                    calendar.add(Calendar.HOUR, -5);
                    calendar.add(Calendar.MINUTE, -40);
                    break;
                case 3:
                    calendar.add(Calendar.YEAR, -2);
                    break;
                case 4:
                    calendar.add(Calendar.DATE, -4);
                    break;
                case 5:
                    calendar.add(Calendar.MONTH, -1);
                    break;
            }
            Date changedDate = calendar.getTime();
            calendar = Calendar.getInstance();
            richMessage = slashSchedule.getScheduleResults("\"Walking\" " +
                    dateFormat.format(changedDate));
            Assert.assertEquals("Error! Requested date is in the past!",
                    richMessage.getText());
            Assert.assertEquals("Please use the current date or a future " +
                    "date!", richMessage.getAttachments()[0].getText());
        }
        SlackBot.resetEventList();

        boolean itExists = false;
        calendar.add(Calendar.DATE, 1);
        String dateToUse = retrieveDate(calendar);
        for (int similarExistsCases = 0; similarExistsCases < 4;
             similarExistsCases++) {
            switch (similarExistsCases) {
                case 0:
                    slashSchedule.getScheduleResults("\"Color run for " +
                            "charity\" "
                            + dateToUse + " 4:24 PM");
                    richMessage = slashSchedule.getScheduleResults("\"Color " +
                            "run for charity\" "
                            + dateToUse + " 4:24 PM");
                    itExists = true;
                    break;
                case 1:
                    calendar.add(Calendar.DATE, 2);
                    dateToUse = retrieveDate(calendar);
                    slashSchedule.getScheduleResults("\"Color " +
                            "run for charity\" "
                            + dateToUse + " 4:24 PM");
                    calendar = calendar.getInstance();
                    calendar.add(Calendar.DATE, 1);
                    dateToUse = retrieveDate(calendar);
                    richMessage = slashSchedule.getScheduleResults("\"Color " +
                            "run for charity\" " + dateToUse + " 4:24 PM");
                    itExists = false;
                    break;
                case 2:
                    calendar.add(Calendar.HOUR, 2);
                    calendar.add(Calendar.MINUTE, 31);
                    SimpleDateFormat timeFormat = new SimpleDateFormat
                            ("hh:mm aaa");
                    String timeString = timeFormat.format(calendar.getTime());
                    slashSchedule.getScheduleResults("\"Color run for " +
                            "charity\" "
                            + dateToUse + " " + timeString);

                    richMessage = slashSchedule.getScheduleResults("\"Color " +
                            "run for charity\" "
                            + dateToUse + " 4:24 PM");

                    itExists = false;
                    break;
                case 3:
                    slashSchedule.getScheduleResults("\"Color run for " +
                            "cancer\" "
                            + dateToUse + " 4:24 PM");
                    richMessage = slashSchedule.getScheduleResults("\"Color " +
                            "run for charity\" "
                            + dateToUse + " 4:24 PM");
                    itExists = false;
            }
            calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 1);
            dateToUse = retrieveDate(calendar);
            if (itExists) {
                errorHead = "Error! This event has already been " +
                        "scheduled!";
                errorAttachment = "You may schedule a similar event if at " +
                        "least one of the event fields (name, date, time) is " +
                        "different.";
                Assert.assertEquals(errorAttachment, richMessage
                        .getAttachments()[0].getText());

            } else {
                String originalDate = retrieveDate(calendar);
                calendar = Calendar.getInstance();
                errorHead = ("Your event has been scheduled: " +
                        "\"Color run for charity\" on " +
                        getDateDynamically(originalDate, calendar, true) +
                        " at 4:24 PM");
                calendar.add(Calendar.DATE, 1);

            }
            Assert.assertEquals(errorHead, richMessage.getText());
            SlackBot.resetEventList();
        }
    }

    @Test
    public void testScheduleRetrieval() {

        SlackBot.resetEventList();
        Calendar calendar = Calendar.getInstance();


        ArrayList<ScheduleEvent> requestedEvents;
        //used to see if the events created for a specific day or range match
        // those returned in the retrieval process
        ArrayList<ScheduleEvent> createdEvents;
        try {
            SlackBot.addScheduleEvent(new ScheduleEvent("\"My event\" " +
                    "Tomorrow 2:32 PM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Event 2\" " +
                    "Tomorrow 5:29 PM"));
            createdEvents = SlackBot.getEventList();
            requestedEvents = SlackBot.getEvents("Tomorrow");
            Assert.assertEquals(2, requestedEvents.size());
            checkRetrieval(createdEvents, requestedEvents, "Tomorrow", null, -1, -1, -1, -1);


            String currentDate = retrieveDate(calendar);
            SlackBot.resetEventList();
            //all the individual day names
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Party time\"" +
                    " Saturday 10:00 PM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Another party " +
                    "time\" Sunday 9:00 PM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Recovery time\"" +
                    " Monday 7:00 AM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Exhausted time\"" +
                    " Tuesday 8:00 AM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Go to work time\"" +
                    " Tuesday 12:00 PM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Coffee time\"" +
                    " Wednesday 7:00 AM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Waiting for party time\"" +
                    " Thursday 11:59 PM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"More party time\"" +
                    " Friday 11:00 PM"));
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Meet up with " +
                    "friends\" Friday 10:00 PM"));
            createdEvents = SlackBot.getEventList();
            Assert.assertEquals(9, createdEvents.size());
            for (Map.Entry<Integer, String> entry : getDayMap().entrySet()) {
                requestedEvents = SlackBot.getEvents(entry.getValue());
                checkRetrieval(createdEvents, requestedEvents, entry.getValue(),
                        null, -1, -1, -1, -1);
            }
            requestedEvents = new ArrayList<>();

            //this week
            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String endRangeDate = "";
            Date beginDate = null;
            try {
                beginDate = dateFormat.parse(currentDate);
                calendar.add(Calendar.DATE, ScheduleEvent.findDayDifference
                        ("Saturday"));
                endRangeDate = retrieveDate(calendar);
                calendar = Calendar.getInstance();
            } catch (ParseException exec) {
                exec.printStackTrace();
            }
            ArrayList<String> requestedRange = SlackBot.populateDayRange
                    (currentDate, endRangeDate);
            for (String dayString : requestedRange) {
                requestedEvents.addAll(SlackBot.getEvents(dayString));
            }
            checkRetrieval(createdEvents, requestedEvents, null,
                    requestedRange, -1, -1, -1, -1);
            requestedEvents = new ArrayList<>();
            requestedRange = new ArrayList<>();


            //next week
            int daysToAdd;
            int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dayOfWeek < 4) {
                daysToAdd = 8;
            } else {
                daysToAdd = 5;
            }
            calendar.add(Calendar.DATE, daysToAdd);
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Reading\"" +
                    " " + retrieveDate(calendar) + " 9:45 AM"));
            calendar.add(Calendar.DATE, 1);
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Walk\"" +
                    " " + retrieveDate(calendar) + " 1:02 PM"));
            calendar = Calendar.getInstance();
            try {
                calendar.add(Calendar.DATE, ScheduleEvent.findDayDifference
                        ("Sunday"));
                currentDate = retrieveDate(calendar);
                beginDate = dateFormat.parse(currentDate);
                calendar.add(Calendar.DATE, ScheduleEvent.findDayDifference
                        ("Saturday"));
                endRangeDate = retrieveDate(calendar);
                calendar = Calendar.getInstance();
            } catch (ParseException exec) {
                exec.printStackTrace();
            }
            calendar.add(Calendar.DATE, ScheduleEvent.findDayDifference
                    ("Sunday"));
            requestedRange = SlackBot.populateDayRange(currentDate, endRangeDate);
            calendar = Calendar.getInstance();
            /*
             * add  all the events of each day of the requestedRange list to
             * the requestedEvents list
             */
            for (String dayString : requestedRange) {
                requestedEvents.addAll(SlackBot.getEvents(dayString));
            }
            checkRetrieval(createdEvents, requestedEvents, null,
                    requestedRange, -1, -1, -1, -1);
            requestedEvents = new ArrayList<>();
            requestedRange = new ArrayList<>();


            //this month
            daysToAdd = 0;
            if (calendar.get(calendar.get(Calendar.DATE)) < 15) {
                daysToAdd = 10;
            } else if ((calendar.get(Calendar.MONTH) == 1 && calendar
                    .get(Calendar.DATE) < 27) || (calendar.get(Calendar.MONTH)
                    != 1 && calendar.get(Calendar.DATE) < 30)) {
                daysToAdd = 1;
            }

            calendar.add(Calendar.DATE, daysToAdd);
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Running\"" +
                    " " + retrieveDate(calendar) + " 3:19 PM"));
            if (daysToAdd == 10) {
                daysToAdd -= 1;
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, daysToAdd);
                SlackBot.addScheduleEvent(new ScheduleEvent("\"Workout\"" +
                        " " + retrieveDate(calendar) + " 10:46 AM"));
            }
            calendar = Calendar.getInstance();
            int thisMonth = calendar.get(Calendar.MONTH) + 1;
            for (ScheduleEvent scheduleEvent : createdEvents) {
                //if the month of the event matches this month
                int temp = ScheduleEvent.getDateFromText(scheduleEvent
                        .getEventDate())[0];
                if (ScheduleEvent.getDateFromText(scheduleEvent.getEventDate()
                )[0] == thisMonth) {
                    requestedEvents.add(scheduleEvent);
                }
            }
            checkRetrieval(createdEvents, requestedEvents, null,
                    null, thisMonth, -1, -1, -1);
            requestedEvents = new ArrayList<>();

            //next month
            calendar.add(Calendar.MONTH, 1);
            if (calendar.get(calendar.get(Calendar.DATE)) < 15) {
                daysToAdd = 6;
            } else if ((calendar.get(Calendar.MONTH) == 1 && calendar
                    .get(Calendar.DATE) < 27) || (calendar.get(Calendar.MONTH)
                    != 1 && calendar.get(Calendar.DATE) < 30)) {
                daysToAdd = 1;
            }
            calendar.add(Calendar.DATE, daysToAdd);
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Dinner\"" +
                    " " + retrieveDate(calendar) + " 5:00 PM"));
            if (daysToAdd == 6) {
                daysToAdd -= 1;
                calendar = Calendar.getInstance();
                calendar.add(Calendar.DATE, daysToAdd);
                SlackBot.addScheduleEvent(new ScheduleEvent("\"My " +
                        "Birthday\"" + " " + retrieveDate(calendar) + " 12:00 AM"));
            }
            calendar = Calendar.getInstance();
            int nextMonth = thisMonth + 1;
            if (nextMonth > 12) {
                nextMonth = 1;
            }
            for (ScheduleEvent scheduleEvent : createdEvents) {
                //if the month of the event matches next month
                if (ScheduleEvent.getDateFromText(scheduleEvent.getEventDate()
                )[0] == nextMonth) {
                    requestedEvents.add(scheduleEvent);
                }
            }
            checkRetrieval(createdEvents, requestedEvents, null,
                    null, -1, nextMonth, -1, -1);
            requestedEvents = new ArrayList<>();

            //this year
            int thisYear = calendar.get(Calendar.YEAR);
            for (ScheduleEvent scheduleEvent : createdEvents) {
                //if the month of the event matches this year
                if (ScheduleEvent.getDateFromText(scheduleEvent.getEventDate()
                )[2] == thisYear) {
                    requestedEvents.add(scheduleEvent);
                }
            }
            checkRetrieval(createdEvents, requestedEvents, null,
                    null, -1, -1, thisYear, -1);
            requestedEvents = new ArrayList<>();


            //next year
            calendar.add(Calendar.YEAR, 1);
            if (calendar.get(Calendar.MONTH) <= 10 && calendar.get(Calendar
                    .DATE) < 21) {
                calendar.add(Calendar.MONTH, 1);
                calendar.add(Calendar.DATE, 6);
                SlackBot.addScheduleEvent(new ScheduleEvent("\"Breakfast " +
                        "at IHOP\" " + retrieveDate(calendar) + " 8:59 AM"));
            }
            calendar.add(Calendar.DATE, 1);
            SlackBot.addScheduleEvent(new ScheduleEvent("\"Writing class\"" +
                    " " + retrieveDate(calendar) + " 3:15 PM"));
            calendar = Calendar.getInstance();
            int nextYear = thisYear + 1;
            for (ScheduleEvent scheduleEvent : createdEvents) {
                //if the month of the event matches next year
                if (ScheduleEvent.getDateFromText(scheduleEvent.getEventDate()
                )[2] == nextYear) {
                    requestedEvents.add(scheduleEvent);
                }
            }
            checkRetrieval(createdEvents, requestedEvents, null,
                    null, -1, -1, -1, nextYear);

        } catch (ScheduleException exec) {
            exec.printStackTrace();
        }
    }

    // takes care of all the retrieval assertions
    public void checkRetrieval(ArrayList<ScheduleEvent> createdEvents,
                               ArrayList<ScheduleEvent> requestedEvents,
                               String requestedDate, ArrayList<String>
                                       requestedRange, int thisMonth, int
                                       nextMonth, int thisYear, int nextYear) {

        Calendar calendar = Calendar.getInstance();
        int eventMonth = -1;
        int eventYear = -1;
        String lowerCaseDate = "";
        if (requestedDate != null) {
            lowerCaseDate = requestedDate.toLowerCase();
        }
        //loops through all the requested events and assures that each
        // requested event matches an event of the
        // matches
        int desiredEventsCount = 0;
        for (int currentIndex = 0; currentIndex < createdEvents.size();
             currentIndex++) {
            String desiredDateUnformatted = "";

            ScheduleEvent currentEvent = createdEvents.get(currentIndex);
            if (requestedRange == null && (lowerCaseDate.equals
                    ("tomorrow") ||
                    lowerCaseDate.equals("today")
                    || getDayMap().containsValue(lowerCaseDate))) {
                desiredDateUnformatted = getDateDynamically(lowerCaseDate,
                        calendar, false);
            } else if (requestedRange == null && requestedDate != null) {
                desiredDateUnformatted = lowerCaseDate;
            } else if (thisMonth != -1 || nextMonth != -1) {
                eventMonth = currentEvent.getDateFromText(currentEvent
                        .getEventDate())[0];
            } else if (thisYear != -1 || nextYear != -1) {
                eventYear = currentEvent.getDateFromText(currentEvent
                        .getEventDate())[2];
            }
            /* if the current event in the list of created events matches
             * the desired date or is in the desired date range
              */

            if ((requestedDate != null && currentEvent.getEventDate()
                    .equals(desiredDateUnformatted)) || (requestedRange != null &&
                    requestedRange.contains(currentEvent.getEventDate())) ||
                    (thisMonth != -1 && eventMonth == thisMonth) ||
                    (nextMonth != -1 && eventMonth == nextMonth) ||
                    (thisYear != -1 && eventYear == thisYear) ||
                    (nextYear != -1 && eventYear == nextYear)) {
                desiredEventsCount += 1;
                /* assert that the event was retrieved in the
                 * requestedEvents list
                 */
                Assert.assertEquals(true, requestedEvents.contains
                        (currentEvent));
            }
            /* assures that there are not anymore events in the requested
             * list than the desired events in the createdEvents list as
             * more or less would indicate too many or too little
             * events were returned that match the requested date
             */
            if (currentIndex == (createdEvents.size() - 1)) {
                Assert.assertEquals(true, desiredEventsCount ==
                        requestedEvents.size());

            }
        }


    }

    /*
     * gets the current day in string form while also making sure it parses
     * correctly
     */
    public String retrieveDate(Calendar calendar) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date currentDate = null;
        String currentDateString = "";
        try {
            currentDate = dateFormat.parse((calendar.get
                    (Calendar.MONTH) + 1) + "/" + calendar
                    .get(Calendar.DAY_OF_MONTH) + "/" + calendar
                    .get(Calendar.YEAR));
            currentDateString = dateFormat.format(currentDate);

        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return currentDateString;
    }

    //gets date string of a specified day of an event to be scheduled
    public String getDateDynamically(String day, Calendar givenCalendar, boolean
            getFormatted) {
        Calendar calendar = (Calendar) givenCalendar.clone();
        int dayDifference;
        String dateSuffix, dateText, reformattedDateText = "";
        int currentDayNumber;
        Date currentDate = null;
        SimpleDateFormat inputFormat;
        day = day.toLowerCase();
        dayDifference = ScheduleEvent.findDayDifference(day);
        calendar.add(Calendar.DATE, dayDifference);
        dateText = retrieveDate(calendar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            currentDate = dateFormat.parse(dateText);
        } catch (ParseException excep) {
            excep.printStackTrace();
        }
        if (!getFormatted) {
            return dateFormat.format(currentDate);
        }
        currentDayNumber = ScheduleEvent.getDateFromText(dateText)[1];
        dateSuffix = SlackBot.getDayNumberSuffix(currentDayNumber);
        inputFormat = new SimpleDateFormat
                ("EEEEEEEEE, MMMMMMMMM d'" + dateSuffix + "', yyyy");
        try {
            Date parsedDate = dateFormat.parse(dateText);
            reformattedDateText = inputFormat.format(parsedDate);
        } catch (ParseException execept) {
            execept.printStackTrace();
        }
        return reformattedDateText;

    }

    public ArrayList<String> getRangeDateStrings() {
        ArrayList<String> rangeStrings = new ArrayList<>();
        rangeStrings.add("this week");
        rangeStrings.add("next week");
        rangeStrings.add("this month");
        rangeStrings.add("next month");
        rangeStrings.add("this year");
        rangeStrings.add("next year");
        return rangeStrings;
    }

    public HashMap<Integer, String> getDayMap() {
        HashMap<Integer, String> dayMap = new HashMap<>();
        dayMap.put(1, "sunday");
        dayMap.put(2, "monday");
        dayMap.put(3, "tuesday");
        dayMap.put(4, "wednesday");
        dayMap.put(5, "thursday");
        dayMap.put(6, "friday");
        dayMap.put(7, "saturday");
        return dayMap;
    }
}
