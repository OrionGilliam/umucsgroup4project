package bot.slack;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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



@RestController
public class SlashDateTime{

    private String timeZone;
    private LocalDateTime datetime;
    private DateTimeFormatter dTF = DateTimeFormatter.ofPattern(
                "'It is' hh:mma 'on' dd MMM',' yyyy'.'");
    private RichMessage richMessage;
    private static final Logger logger = 
            LoggerFactory.getLogger(SlashDateTime.class);

    // gets token to validate command came from an authorized slack.com location
    @Value("${SlashDateTimeToken}")
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
    @RequestMapping(value = "/datetime",
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
        // Validate token
        if (!token.equals(slackToken)) {
            return new RichMessage("Invalid Slack Command Token.");
        } // end if

        // Check/get the timezone and build the response
        if (ZoneId.SHORT_IDS.containsKey(text.toUpperCase())) {
            datetime = LocalDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get(text.toUpperCase())));
            richMessage = new RichMessage(dTF.format(datetime));
        } else {
            richMessage = new RichMessage("Please use the format /datetime"
                    + " <time zone>.  (ex. /datetime est)");
        } // end if/else

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
    } // end RichMessage method
} //end SlashDateTime class
