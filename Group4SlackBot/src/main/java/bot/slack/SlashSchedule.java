package bot.slack;

import bot.common.ErrorMessaging;
import bot.schedule.ScheduleEvent;
import bot.schedule.ScheduleException;
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

import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@RestController
public class SlashSchedule {
    private static final Logger logger = LoggerFactory.getLogger
            (SlashSchedule.class);
    /**
     * The token you get while creating a new Slash Command. You
     * should paste the token in application.properties file.
     */
    @Value("${slashScheduleToken}")
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
    @RequestMapping(value = "/schedule", method = RequestMethod.POST,
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
        return getScheduleResults(text).encodedMessage();

    }
    public static RichMessage getScheduleResults(String enteredText) {
        RichMessage richMessage = new RichMessage();
        try {
            ScheduleEvent scheduleEvent = new ScheduleEvent(enteredText);
            if (SlackBot.getEventList().contains(scheduleEvent)) {
                throw new ScheduleException("AlreadyExists");
            }
            if(timeInPast(scheduleEvent.getEventTime()) ){
                throw new ScheduleException("PastDate");
            }
            SlackBot.addScheduleEvent(scheduleEvent);
            richMessage.setText("Your event has been scheduled: " +
                    scheduleEvent.toString());
        } catch (ScheduleException exec) {
            ErrorMessaging.setErrorMessage(exec, richMessage);
        }

        return richMessage;
    }

    public static boolean timeInPast(String requestedTime) {
        SimpleDateFormat timeFormatter = new SimpleDateFormat("h:mm aaa");
        Date currentTime = null;
        try {
            currentTime = timeFormatter.parse(timeFormatter.format(Calendar
                    .getInstance().getTime()));
        } catch (ParseException exec) {
            exec.printStackTrace();
        }
        try {
            if (currentTime.after(timeFormatter.parse(requestedTime))) {
                return true;
            }
        } catch (ParseException ex) {
            ex.printStackTrace();
        }
        return false;
    }
}
