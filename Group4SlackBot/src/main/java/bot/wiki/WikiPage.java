package bot.wiki;
/** Represents an extracted wikipedia page. Used by Gson to retrieve specific
 * wiki fields from the json that is returned by the MediaWiki api
 */
public class WikiPage {
    public String title;
    public String extract;
    public String fullurl;
}
