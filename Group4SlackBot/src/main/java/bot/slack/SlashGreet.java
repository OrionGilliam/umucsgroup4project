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
public class SlashGreet{

    private int numGreets = 0;
    private static final Logger logger = 
            LoggerFactory.getLogger(BasicSlashCommandTemplate.class);

    // gets token to validate command came from an authorized slack.com location
    @Value("${SlashGreetToken}")
    private String slackToken;

    // validates command values
    @RequestMapping(value = "/greet",
            method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public RichMessage onReceiveSlashCommand(String token, String teamId, 
            String teamDomain, String channelId, String channelName, 
            String userId, String userName, String command, String text, 
            String responseUrl) {
        // validate token
        if (!token.equals(slackToken)) {
            return new RichMessage("Invalid Slack Command Token.");
        } // end if

        // Build the response
        RichMessage richMessage = new RichMessage("Hello " + userName + "!" 
                + " Welcome to " + channelName + ".");
        richMessage.setResponseType("in_channel");
        
        // Attachments for the bottom of the response
        Attachment[] attachments = new Attachment[1];
        attachments[0] = new Attachment();
        attachments[0].setText("I have been greeted (" + ++numGreets + ") times"
                + " this session.");
        richMessage.setAttachments(attachments);

        // For debugging purposes
        if (logger.isDebugEnabled()) {
            try {
                logger.debug("Reply (RichMessage): {}", 
                        new ObjectMapper().writeValueAsString(richMessage));
            } catch (JsonProcessingException e) {
                logger.debug("Error parsing RichMessage: ", e);
            } // end t/c
        } // end if

        // output the reply
        return richMessage.encodedMessage();
    }
}
