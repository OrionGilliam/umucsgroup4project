package bot.common;

import bot.schedule.ScheduleException;
import me.ramswaroop.jbot.core.slack.models.Attachment;
import me.ramswaroop.jbot.core.slack.models.RichMessage;

public class ErrorMessaging {
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
}
