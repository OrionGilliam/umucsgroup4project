package bot.slack;
/** Represents an extracted wikipedia page. Used by Gson to retrieve specific
 * wiki fields from the json that is returned by the MediaWiki api
 */
public class WikiPage {
    String title;
    String extract;
    String fullurl;
}
