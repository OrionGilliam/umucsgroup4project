package bot.slack;

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

import java.util.Calendar;

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

    public static void setErrorMessage(ScheduleException exec, RichMessage
            richMessage) {
        Attachment errorFix = new Attachment();
        switch (exec.getFailureReason()) {
            case "EventFormat":
                richMessage.setText("Error! Incorrect event name format!");
                errorFix.setText("The event name must be within quotes. " +
                        "Spaces must also be put between event name, date and" +
                        " time.");
                break;
            case "DateFormat":
                richMessage.setText("Error! Incorrect date format!");
                errorFix.setText("The event date must be either a day name " +
                        "(Sunday - Saturday), today, tomorrow, this week, " +
                        "next week, this month, next month, this " +
                        "year, next year, or a specific date in the " +
                        "format mm/dd/yyyy. (ex. 04/15/2019). Spaces must " +
                        "also be put between event name, date and " +
                        "time.");

                break;
            case "TimeFormat":
                richMessage.setText("Error! Incorrect time format!");
                errorFix.setText("The event time must be in the form of " +
                        "\"hh:mm aaa\" where h is the hour(number), m is the " +
                        "minute(number) and aaa m is either AM or PM (ex. " +
                        "8:42 AM). Spaces must also be put between event name, " +
                        "date and time.");
                break;
            case "PastDate":
                richMessage.setText("Error! Requested date is in the past!");
                errorFix.setText("Please use the current date or a future " +
                        "date!");
                break;
            case "AlreadyExists":
                richMessage.setText("Error! This event has already been " +
                        "scheduled!");
                errorFix.setText("You may schedule a similar event if at " +
                        "least one of the event fields (name, date, time) is " +
                        "different.");
        }
        Attachment[] attachments = new Attachment[5];
        attachments[0] = errorFix;
        richMessage.setAttachments(attachments);
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
        String date, eventName, time;
        RichMessage richMessage = new RichMessage();
        Calendar calendar = Calendar.getInstance();


        try {
            ScheduleEvent scheduleEvent = new ScheduleEvent(text);
            if (SlackBot.getEventList().contains(scheduleEvent)) {
                throw new ScheduleException("AlreadyExists");
            }
            SlackBot.addScheduleEvent(scheduleEvent);
            richMessage.setText("Your event has been scheduled: " +
                    scheduleEvent.toString());
        } catch (ScheduleException exec) {
            setErrorMessage(exec, richMessage);
        }

        return richMessage;

    }
}
