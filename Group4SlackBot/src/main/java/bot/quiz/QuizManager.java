package bot.quiz;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class QuizManager {


    private List<ActiveQuiz> activeQuizes;
    private final int NUMBER_QUESTIONS_ASKED;
    private static final Logger logger = LoggerFactory.getLogger(QuizManager.class);

    public QuizManager(int totalQuestions) {
        this.NUMBER_QUESTIONS_ASKED = totalQuestions;


        try {

            File folder = new File(".");

            //listFiles(folder.listFiles());


            File file = new File("/app/./Group4SlackBot/target/classes/quiz.json");

            //File file = new File(ClassLoader.getSystemResource("quiz.json").getFile());
            byte[] bytes = Files.readAllBytes(file.toPath());
            JSONObject obj = new JSONObject(new String(bytes, "UTF-8"));
            new QuizQuestions(obj);

            activeQuizes = new ArrayList<>();
        }catch(Exception e){
            logger.error(e.getMessage(), e);
        }
    }

    public String conductQuiz(String userName, String text){

        String response = "";

        if(text.equalsIgnoreCase("trivia")){
            if(checkUserName(userName)){
                ActiveQuiz currentUser = getUserQuiz(userName);
                response = "You are actively in the quiz, please select an answer " + currentUser.askQuestion();
            }else{
                activeQuizes.add(new ActiveQuiz(userName));
                ActiveQuiz currentQuiz = getUserQuiz(userName);
                response = currentQuiz.askQuestion();
            }
        }else if(text.matches("a|b|c|d")){
            if(checkUserName(userName)){
                ActiveQuiz currentUser = getUserQuiz(userName);
                response = currentUser.answerQuestion(text);
                int correct = currentUser.getCorrect();
                int wrong = currentUser.getWrong();
                int remainingQuestions = NUMBER_QUESTIONS_ASKED - correct - wrong;
                response += "\n Number correct: " + correct;
                response += "\n Number incorrect: " + wrong;
                response += "\n Remaining questions: " + remainingQuestions;

                if(remainingQuestions < 1){
                    activeQuizes.remove(currentUser);
                    response += "\n Thank you for playing, your game is over";
                }else{
                    response += "\n" + getUserQuiz(userName).askQuestion();
                }
            }else{
                response = "You have not entered a valid command for the quiz, please try 'trivia'";
            }
        } else{
            response = "You have not entered a valid command for the quiz, please try 'trivia'";
        }

        return response;
    }

    private boolean checkUserName(String distinctUser){
        for(ActiveQuiz user : activeQuizes){
            if(distinctUser.equals(user.getUserName())){
                return true;
            }
        }
        return false;
    }

    private ActiveQuiz getUserQuiz(String distinctUser){
        for(ActiveQuiz user : activeQuizes){
            if(user.getUserName().equals(distinctUser)){
                return user;
            }
        }
        return null;
    }

    private void listFiles(File[] listOfFiles){
        for (int i = 0; i < listOfFiles.length; i++) {
           if(listOfFiles[i].getName().equalsIgnoreCase("quiz.json")){
               logger.error("It has been FOUND FOUND FOUND FOUND: " + listOfFiles[i].getAbsolutePath());
           }else if (listOfFiles[i].isFile()) {
                logger.error("File " + listOfFiles[i].getName());
            } else if (listOfFiles[i].isDirectory()) {
                logger.error("Directory " + listOfFiles[i].getName());
                listFiles(listOfFiles[i].listFiles());
            }
        }
    }
}
