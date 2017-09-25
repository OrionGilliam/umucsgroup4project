package bot.quiz;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class QuizQuestions {
    private static Map<Integer, JSONObject> questions;

    QuizQuestions(JSONObject quizQuestions){
        questions = quizQuestions(quizQuestions);
    }

   /* public static Map<Integer, JSONObject> getQuestions(){
        return questions;
    }*/

    static JSONObject getQuestion(int questionNumber){
        return questions.get(questionNumber);
    }

    static int numberQuestions(){
        return questions.size();
    }

    private Map<Integer, JSONObject> quizQuestions(JSONObject quizQuestions){
        int mapKey = 1;
        Map<Integer, JSONObject> quizes = new HashMap<>();
        for(String key : quizQuestions.keySet()){
            quizes.put(mapKey, quizQuestions.getJSONObject(key));
            mapKey++;
        }
        return quizes;
    }
}
