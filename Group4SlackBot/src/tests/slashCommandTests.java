import bot.dieroller.DieRoller;
import bot.quiz.QuizManager;
import bot.slack.SlashDieRoller;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
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

        //verifies that two questions are asked and that the user is thanked at the end
       qm.conductQuiz(legitUser, "trivia");
        qm.conductQuiz(legitUser, "a");
        Assert.assertTrue( qm.conductQuiz(legitUser, "b").contains("Thank you"));

    }
}
