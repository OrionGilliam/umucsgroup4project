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

@RestController
public class BasicSlashCommandTemplate{


    private static final Logger logger = LoggerFactory.getLogger(BasicSlashCommandTemplate.class);

    //Insert your meaningful token name here
    @Value("${SlashMeaningfulCommandToken}")
    private String slackToken;


    //insert slash command value here
    @RequestMapping(value = "/Meaningful-Command",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommand(String token, String teamId, String teamDomain, String channelId, String channelName, String userId, String userName, String command, String text, String responseUrl) {
        // validate token
        if (!token.equals(slackToken)) {
            return new RichMessage("Sorry! You're not lucky enough to use our slack command.");
        }

        /** build response */

        //inserts initial greeting/first line of response
        RichMessage richMessage = new RichMessage("This is my meaningful command greeting");
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
        }

        return richMessage.encodedMessage(); // don't forget to send the encoded message to Slack
    }
}
