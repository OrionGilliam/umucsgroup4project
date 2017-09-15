package bot.slack;

import bot.quiz.ActiveQuiz;
import bot.quiz.QuizQuestions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;



@RestController
public class SlashQuiz {


    private static final Logger logger = LoggerFactory.getLogger(SlashQuiz.class);

    //Insert your meaningful token name here
    @Value("${slashQuizToken}")
    private String slackToken;
    private List<ActiveQuiz> activeQuizes;
    private int totalQuestions = 2;


    public SlashQuiz() throws IOException {
        ClassLoader classLoad = getClass().getClassLoader();
        File file = new File(classLoad.getResource("quiz.json").getFile());
        byte[] bytes = Files.readAllBytes(file.toPath());
        JSONObject obj = new JSONObject(new String(bytes, "UTF-8"));
        new QuizQuestions(obj);
        this.activeQuizes = new ArrayList<ActiveQuiz>();
    }

    /**
     * Slash Command handler. When a user types for example "/app help"
     * then slack sends a POST request to this endpoint. So, this endpoint
     * should match the url you set while creating the Slack Slash Command.
     *
     * @param token
     * @param teamId
     * @param teamDomain
     * @param channelId
     * @param channelName
     * @param userId
     * @param userName
     * @param command
     * @param text
     * @param responseUrl
     * @return
     */
    //insert slash command value here
    @RequestMapping(value = "/quiz",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommand(@RequestParam("token") String token,
                                             @RequestParam("team_id") String teamId,
                                             @RequestParam("team_domain") String teamDomain,
                                             @RequestParam("channel_id") String channelId,
                                             @RequestParam("channel_name") String channelName,
                                             @RequestParam("user_id") String userId,
                                             @RequestParam("user_name") String userName,
                                             @RequestParam("command") String command,
                                             @RequestParam("text") String text,
                                             @RequestParam("response_url") String responseUrl) {
        // validate token
        if (!token.equals(slackToken)) {
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }


        /** build response */

        String response = "";

        if(text.equalsIgnoreCase("trivia")){
            if(checkUserName(userName)){
                ActiveQuiz currentUser = getUserQuiz(userName);
                response = "You are actively in the quiz, please select an answer " + currentUser.askQuestion();
            }else{
                activeQuizes.add(new ActiveQuiz(userName));
               ActiveQuiz currentQuiz = getUserQuiz(userName);
               response = currentQuiz.askQuestion();
            }
        }else if(text.matches("a|b|c|d")){
            if(checkUserName(userName)){
                ActiveQuiz currentUser = getUserQuiz(userName);
                response = currentUser.answerQuestion(text);
                int correct = currentUser.getCorrect();
                int wrong = currentUser.getWrong();
                int remainingQuestions = totalQuestions - correct - wrong;
                response += "\n Number correct: " + correct;
                response += "\n Number incorrect: " + wrong;
                response += "\n Remaining questions: " + remainingQuestions;
                if(remainingQuestions > 1){
                    activeQuizes.remove(currentUser);
                }
            }
        } else{
            response = "You have not entered a valid command for the quiz, please try 'trivia'";
        }

        //inserts initial greeting/first line of response
        /*RichMessage richMessage = new RichMessage("This is my meaningful command greeting");
        richMessage.setResponseType("in_channel");
        // set attachments.  Assure that it is set to the number of lines you intend to add to the response
        Attachment[] attachments = new Attachment[1];
        attachments[0] = new Attachment();
        attachments[0].setText("I am the rest of the message from the slash command.");
        richMessage.setAttachments(attachments);

        // For debugging purpose only
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Reply (RichMessage): {}", new ObjectMapper().writeValueAsString(richMessage));
            } catch (JsonProcessingException e) {
                logger.debug("Error parsing RichMessage: ", e);
            }
        }*/

        //return richMessage.encodedMessage(); // don't forget to send the encoded message to Slack
        return formResponse(response).encodedMessage();
    }

    private RichMessage formResponse(String response){
        return new RichMessage(response);
    }

    private boolean checkUserName(String distinctUser){
        for(ActiveQuiz user : activeQuizes){
            if(distinctUser.equals(user.getUserName())){
                return true;
            }
        }
        return false;
    }

    private ActiveQuiz getUserQuiz(String distinctUser){
        for(ActiveQuiz user : activeQuizes){
            if(user.getUserName().equals(distinctUser)){
                return user;
            }
        }
        return null;
    }

}
