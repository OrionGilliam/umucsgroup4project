package bot.slack;

import bot.quiz.ActiveQuiz;
import bot.quiz.QuizManager;
import bot.quiz.QuizQuestions;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
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
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;



@RestController
public class SlashQuiz {


    private static final Logger logger = LoggerFactory.getLogger(SlashQuiz.class);

    //Insert your meaningful token name here
    @Value("${slashQuizToken}")
    private String slackToken;

    private QuizManager qm;

    private final int QUESTIONS_TO_ASK = 2;

    public SlashQuiz() throws IOException {
     this.qm = new QuizManager(QUESTIONS_TO_ASK);
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


        response = qm.conductQuiz(userName, text);

        return formResponse(response).encodedMessage();
    }

    private RichMessage formResponse(String response){
        return new RichMessage(response);
    }



}
