package bot.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class SlashDieRoller {



    private static final Logger logger = LoggerFactory.getLogger(SlashDieRoller.class);

    //Insert your meaningful token name here
    @Value("${slashDieRollToken}")
    private String slackToken;
    private Random rand = new Random();

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
    @RequestMapping(value = "/roll",
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


        String dice = text;
        String returnResult = "Dice rolled: ";

        try{
            String[] splitdice = dice.split("d");
            int numbDice = Integer.parseInt(splitdice[0]);
            int numbSides = Integer.parseInt(splitdice[1]);

            int[] results = new int[numbDice];
            int total = 0;



            for (int i = 0; i < results.length; i++){
                results[i] = rand.nextInt(numbSides) + 1;

                if(i ==0){
                    returnResult += results[i];
                }
                else{
                    returnResult += ", " + results[i];
                }
                total += results[i];
            }

            returnResult += ".  Total: " + total;
        }catch(Exception e){
            returnResult = "Dice format error";
        }


        //inserts initial greeting/first line of response
        RichMessage richMessage = new RichMessage("Dice Roller");
        richMessage.setResponseType("in_channel");
        // set attachments.  Assure that it is set to the number of lines you intend to add to the response
        Attachment[] attachments = new Attachment[1];
        attachments[0] = new Attachment();
        attachments[0].setText(returnResult);
        richMessage.setAttachments(attachments);

        // For debugging purpose only
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Reply (RichMessage): {}", new ObjectMapper().writeValueAsString(richMessage));
            } catch (JsonProcessingException e) {
                logger.debug("Error parsing RichMessage: ", e);
            }
        }

        return richMessage.encodedMessage(); // don't forget to send the encoded message to Slack
    }
}
