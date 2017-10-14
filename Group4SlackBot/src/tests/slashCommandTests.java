import bot.datetime.DateTimeChecker;
import bot.dieroller.DieRoller;
import bot.help.HelpMe;
import bot.quiz.QuizManager;
import bot.schedule.ScheduleEvent;
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
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

public class slashCommandTests {

    Properties props;

    @Before
    public void loadProps(){


    }

    @Test
    public void dieRollerTest(){
        Assert.assertNotEquals(DieRoller.rollByString("3d6"), "Dice format error");
        Assert.assertNotEquals(DieRoller.rollByString("38d69"), "Dice format error");
        Assert.assertNotEquals(DieRoller.rollByString("12D8"), "Dice format error");
        Assert.assertEquals(DieRoller.rollByString("howdy"), "Dice format error");
        Assert.assertEquals(DieRoller.rollByString("b6d8"), "Dice format error");
    }

    @Test
    public void quizTest(){
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

        QuizManager qm = new QuizManager(numbQuestions,questions);

        //verifies bad information is given error return
        Assert.assertEquals(qm.conductQuiz(legitUser, "boo loo"), "You have not entered a valid command for the quiz, please try 'trivia'");

        //verifies that two questions are asked and that the user is thanked at the end.  Done with concurrent users
       qm.conductQuiz(legitUser, "trivia");
        qm.conductQuiz(legitUser2, "trivia");
        qm.conductQuiz(legitUser, "a");
        qm.conductQuiz(legitUser2, "a");
        Assert.assertTrue( qm.conductQuiz(legitUser, "b").contains("Thank you"));
        Assert.assertTrue( qm.conductQuiz(legitUser2, "b").contains("Thank you"));
    }

    @Test
    public void wikiTest() {
        RichMessage resultHolder;
        SlashWiki slashWiki = new SlashWiki();
        Assert.assertEquals(slashWiki.getWikiResults("Bsadas").getText(),
                "I'm sorry! I could not find a wikipedia page with a title " +
                        "of \"Bsadas\".");

        Assert.assertEquals((resultHolder = slashWiki.getWikiResults("barack " +
                "obama")).getText(), "Barack obama");
        Assert.assertEquals(resultHolder.getAttachments()[0].getText(), "From a " +
                "miscapitalisation: " +
                "This is a redirect from a miscapitalisation. The correct form is given " +
                "by the target of the redirect.");
        resultHolder = null;
        Assert.assertEquals((resultHolder = slashWiki.getWikiResults("barack " +
                "Obama")).getText
                (), "Barack Obama");
        Assert.assertEquals(resultHolder.getAttachments()[0].getText(), "Barack " +
                "Hussein Obama II (US:  bə-RAHK hoo-SAYN oh-BAH-mə; born August 4, " +
                "1961) is an American politician who served as the 44th President " +
                "of the United States from 2009 to 2017. He is the first African American " +
                "to have served as president.");
        Assert.assertEquals(resultHolder.getAttachments()[1].getText(), "https://en.wikipedia.org/wiki/Barack_Obama");
        Assert.assertEquals(slashWiki.getWikiResults("").getText(), "Error! Please " +
                "enter something to query " +
                "wikipedia for.");
        Assert.assertEquals(slashWiki.getWikiResults("    ").getText(), "Error! " +
                "Please enter something to query " +
                "wikipedia for.");
    }

    @Test
    public void testDateTime(){
        RichMessage mess = DateTimeChecker.checkZone("est");
        Assert.assertTrue(mess.getText().contains("It is"));

        mess = DateTimeChecker.checkZone("blaruug");
        Assert.assertTrue(mess.getText().contains("Please use the format"));
    }

    @Test
    public void testHelpCommand(){
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
    public void testScheduleCommand(){
        RichMessage richMessage;
        String eventText;
        SlackBot slackBot = new SlackBot();
        String reformattedDateText = "";
        SlashSchedule slashSchedule = new SlashSchedule();

        Calendar calendar = Calendar.getInstance();


        richMessage = slashSchedule.getScheduleResults("\"eventName\" Sunday 3:25 AM");
        reformattedDateText = getDateDynamically("Sunday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                "scheduled: \"eventName\" on " + reformattedDateText + " at " +
                "3:25" + " AM");
        calendar = Calendar.getInstance();


        richMessage = slashSchedule.getScheduleResults("\"new event\" Monday 7:48 PM");
        reformattedDateText = getDateDynamically("Monday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                        "scheduled: \"new event\" on " + reformattedDateText +
                " at 7:48 PM");
        calendar = Calendar.getInstance();




        richMessage = slashSchedule.getScheduleResults("\"Business meeting\" Tuesday 9:25 AM");
        reformattedDateText = getDateDynamically("Tuesday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                "scheduled: \"Business meeting\" on " + reformattedDateText +
                " at 9:25 AM");
        calendar = Calendar.getInstance();



        richMessage = slashSchedule.getScheduleResults("\"5k Run\" Wednesday 4:40 PM");
        reformattedDateText = getDateDynamically("Wednesday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                "scheduled: \"5k Run\" on " + reformattedDateText +
                " at 4:40 PM");
        calendar = Calendar.getInstance();

        richMessage = slashSchedule.getScheduleResults("\"Coffee at Starbucks\" Thursday " +
                "12:04 PM");
        reformattedDateText = getDateDynamically("Thursday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                "scheduled: \"Coffee at Starbucks\" on " + reformattedDateText +
                " at 12:04 PM");
        calendar = Calendar.getInstance();

        richMessage = slashSchedule.getScheduleResults("\"Homework due\" Friday 8:52 AM");
        reformattedDateText = getDateDynamically("Friday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                "scheduled: \"Homework due\" on " + reformattedDateText +
                " at 8:52 AM");
        calendar = Calendar.getInstance();
        richMessage = slashSchedule.getScheduleResults("\"Gym\" Saturday 4:27 PM");
        reformattedDateText = getDateDynamically("Saturday", calendar);
        Assert.assertEquals( richMessage.getText(), "Your event has been " +
                "scheduled: \"Gym\" on " + reformattedDateText +
                " at 4:27 PM");
        calendar = Calendar.getInstance();






    }
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
    public String getDateDynamically(String day, Calendar calendar) {
        int dayDifference;
        String dateSuffix, dateText, reformattedDateText = "";
        int currentDayNumber;
        Date currentDate = null;
        SimpleDateFormat inputFormat;
        dayDifference = ScheduleEvent.findDayDifference(day);
        calendar.add(Calendar.DATE, dayDifference);
        dateText = retrieveDate(calendar);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        try {
            currentDate= dateFormat.parse(dateText);
        } catch (ParseException excep) {
            excep.printStackTrace();
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
}
