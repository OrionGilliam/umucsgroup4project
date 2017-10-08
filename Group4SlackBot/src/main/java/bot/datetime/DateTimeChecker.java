package bot.datetime;

import me.ramswaroop.jbot.core.slack.models.RichMessage;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeChecker {

    private static DateTimeFormatter dTF = DateTimeFormatter.ofPattern(
            "'It is' hh:mm a 'on' dd MMM',' yyyy'.'");

    public static RichMessage checkZone(String text){
        RichMessage richMessage;
      LocalDateTime datetime;
        // Check/get the timezone and build the response
        if (ZoneId.SHORT_IDS.containsKey(text.toUpperCase())) {
            datetime = LocalDateTime.now(ZoneId.of(ZoneId.SHORT_IDS.get(text.toUpperCase())));
            richMessage = new RichMessage(dTF.format(datetime));
        } else {
            richMessage = new RichMessage("Please use the format /datetime"
                    + " <time zone>.  (ex. /datetime est)");
        } // end if/else

        return richMessage;
    }

}
