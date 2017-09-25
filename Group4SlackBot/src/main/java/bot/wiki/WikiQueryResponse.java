package bot.wiki;

import java.util.ArrayList;
//represents the query object in the json that is returned by the MediaWiki api
public class WikiQueryResponse {
    //represents the list of wiki pages in the json that  is returned by the MediaWiki api
    public ArrayList<WikiPage> pages;
}
