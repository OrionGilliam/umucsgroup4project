package bot.slack;

import com.google.gson.Gson;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Basic code structure for this class created by created by user Ramswaroop
 * at https://github.com/ramswaroop/jbot/tree/master/jbot
 */

/**
 * This class runs the /wiki command in the slack bot.  The /wiki command
 * takes a string (either a single word or multiple words) and queries the
 * wikipedia database for a page that matches that EXACT string. If there is
 * are any kind of grammatical errors in the user's entry, such as extra
 * punctuation, incorrect capitilization, etc.,  the command may not return
 * the expected results, such as a custom "page not found" error,
 * miscapitalization redirection, etc.
 */


@RestController
public class SlackWikiCommand {
    private static final Logger logger = LoggerFactory.getLogger
            (SlackWikiCommand.class);
    /**
     * The token you get while creating a new Slash Command. You
     * should paste the token in application.properties file.
     */
    @Value("${slashWikiToken}")
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
    @RequestMapping(value = "/wiki",
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
        String searchEntry = text;
        //converts multi-term searches to the correct api format for the url
        searchEntry = searchEntry.replaceAll(" ", "%20");
        /*
         * url that uses the MediaWiki api to retrieve query results from
         * wikipedia
         */
        String wikiUrl = "https://en.wikipedia.org/w/api.php?action=query" +
                "&format=json&formatversion=2&prop=extracts%7Cinfo&titles" +
                "=" + searchEntry + "&exsentences=2&exintro=1&explaintext=&inprop=url";
        URLConnection urlConnection = null;
        RichMessage richMessage = new RichMessage("");
        richMessage.setResponseType("in_channel");
        // validate token
        if (!token.equals(slackToken)) {
            System.out.println("bad token!");
            return new RichMessage("Slack token doesnt match expected " +
                    "command token");
        }
        try {
            URL wikiURL = new URL(wikiUrl);
            try {
                urlConnection = wikiURL.openConnection();
                System.out.println("connection to wiki opened");
            } catch (IOException ex) {
                richMessage.setText("An io error occurred with the wiki link!");
                return richMessage.encodedMessage();
            }
            InputStreamReader streamReader;
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder("");
            if (urlConnection != null && urlConnection.getInputStream() != null) {
                streamReader = new InputStreamReader(urlConnection
                        .getInputStream());
                bufferedReader = new BufferedReader(streamReader);
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                Gson gson = new Gson();
                WikiResponse response = gson.fromJson(stringBuilder.toString(),
                        WikiResponse.class);
                if (response.query.pages != null && response.query.pages.size() >
                        0) {
                    Page firstReturnedPage = response.query.pages.get(0);
                    if (firstReturnedPage.extract != null
                            && !firstReturnedPage.extract.equals("")) {
                        richMessage.setText(firstReturnedPage.title);
                        Attachment titleAttachment = new Attachment();
                        titleAttachment.setText(firstReturnedPage.extract);
                        Attachment urlAttachment = new Attachment();
                        urlAttachment.setText(firstReturnedPage.fullurl);
                        Attachment[] attachments = new Attachment[2];
                        attachments[0] = titleAttachment;
                        attachments[1] = urlAttachment;
                        richMessage.setAttachments(attachments);
                    } else {
                        richMessage.setText("I'm sorry! I could not find a" +
                                " " +
                                "wikipedia page with a title of \"" +
                                firstReturnedPage.title + "\".");
                    }
                }
            } else if (urlConnection == null) {
                richMessage.setText("Error! The url connection was null!");
            }
        } catch (MalformedURLException exec) {
            richMessage.setText("The given url is malformed!");
        } catch (IOException ex) {
            richMessage.setText("An IO exception occurred!");
        }
        return richMessage.encodedMessage();
    }
}