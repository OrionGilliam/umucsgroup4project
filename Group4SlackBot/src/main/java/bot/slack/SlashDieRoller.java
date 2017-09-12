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
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class SlashDieRoller {



    private static final Logger logger = LoggerFactory.getLogger(SlashDieRoller.class);

    //Insert your meaningful token name here
    @Value("${slashDieRollToken}")
    private String slackToken;
    private Random rand = new Random();


    //insert slash command value here
    @RequestMapping(value = "/roll",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommand(String token, String teamId, String teamDomain, String channelId, String channelName, String userId, String userName, String command, String text, String responseUrl) {
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
