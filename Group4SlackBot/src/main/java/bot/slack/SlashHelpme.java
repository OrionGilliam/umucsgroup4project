package bot.slack;

import bot.help.HelpMe;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.ramswaroop.jbot.core.slack.models.RichMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
* Basic code structure for this class created by user Ramswaroop
* at https://github.com/ramswaroop/jbot/tree/master/jbot
*/

@RestController
public class SlashHelpme{

    private int numGreets = 0;
    private static final Logger logger = 
            LoggerFactory.getLogger(SlashGreet.class);

    // gets token to validate command came from an authorized slack.com location
    @Value("${SlashHelpmeToken}")
    private String slackToken;

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
    // validates command values
    @RequestMapping(value = "/helpme",
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
            return new RichMessage("Invalid Slack Command Token.");
        } // end if
/*
        // Build the response
        RichMessage richMessage = new RichMessage("Hello " + userName + "." 
                + " Below is a list of available commands:"
                
                + "\n/roll [xdy] - Simulates rolling x number of y capacity "
                + "dice (eg. \"/roll 3d6\" rolls 3, 6 sided dice)"
                
                + "\n/greet - For waking and testing the bot"
                
                + "\n/wiki [search] - Retrieves the search from Wikipedia (eg. "
                + "\"/wiki dogs\" returns the search for dogs)"
                
                + "\n/quiz - Starts a trivia game"
                
                + "\n/datetime [zone] - Retrieves the time for the given 3 "
                + "letter time zone (eg. \"/datetime EST\" returns the date and "
                + "time for Eastern Standard Time)");*/
        RichMessage richMessage = new RichMessage(HelpMe.helpMessageUserGreet(userName));
        richMessage.setResponseType("in_channel");

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
