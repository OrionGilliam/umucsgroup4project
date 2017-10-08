package bot.help;

public class HelpMe {

    public static String helpMessageUserGreet(String userName){
        return "Hello " + userName + "."
                + " Below is a list of available commands:"

                + "\n/roll [xdy] - Simulates rolling x number of y capacity "
                + "dice (eg. \"/roll 3d6\" rolls 3, 6 sided dice)"

                + "\n/greet - For waking and testing the bot"

                + "\n/wiki [search] - Retrieves the search from Wikipedia (eg. "
                + "\"/wiki dogs\" returns the search for dogs)"

                + "\n/quiz - Starts a trivia game"

                + "\n/datetime [zone] - Retrieves the time for the given 3 "
                + "letter time zone (eg. \"/datetime EST\" returns the date and "
                + "time for Eastern Standard Time)";
    }

}
