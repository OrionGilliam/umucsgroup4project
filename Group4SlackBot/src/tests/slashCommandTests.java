import bot.datetime.DateTimeChecker;
import bot.dieroller.DieRoller;
import bot.help.HelpMe;
import bot.quiz.QuizManager;
import bot.slack.SlashDieRoller;
import bot.wiki.WikiPage;
import bot.wiki.WikiResponse;
import com.google.gson.Gson;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
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
    public void wikiTest(){

      /*RichMessage response = runWikiSearch("Albert Einstein");

      boolean foundPage = false;

      response.getText();

      for(Attachment attach : response.getAttachments()){
          if(attach.getText().contains("wikipedia.org"))
              foundPage = true;
      }

        Assert.assertTrue(foundPage);

        //known disambiguation page
        response = runWikiSearch("jon fox");

        foundPage = false;

        if(response.getText().contains("could not find a wikipedia page"))
            foundPage = false;

        try{
            for(Attachment attach : response.getAttachments()){
                if(attach.getText().contains("wikipedia.org"))
                    foundPage = true;
            }
        }catch(Exception e){

        }
        Assert.assertFalse(foundPage);*/

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

    private RichMessage runWikiSearch(String text){

    }
}
