package bot.quiz;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ActiveQuiz {
    private String userName;
    private List<Integer> usedQuestions;
    private int currentQuestion;
    private Random selectQuestion = new Random();
    private int correct=0;
    private int wrong=0;

    ActiveQuiz(String userName){
        this.userName = userName;
        this.correct = 0;
        this.wrong = 0;

        this.usedQuestions = new ArrayList<>();
        newQuestion();
    }

    String answerQuestion(String answer){
        JSONObject que = QuizQuestions.getQuestion(currentQuestion);
        AtomicBoolean reply = new AtomicBoolean(answer.equalsIgnoreCase(que.getString("answer")));

        if(reply.get()){
            correct++;
            usedQuestions.add(currentQuestion);
            newQuestion();
            return "Correct!";
        }
        else{
            wrong++;
            usedQuestions.add(currentQuestion);
            String wrongReply = que.getString(que.getString("answer"));
            newQuestion();
            return "I'm sorry, the correct answer is " + wrongReply;
        }
    }

    String askQuestion(){
        JSONObject question = QuizQuestions.getQuestion(currentQuestion);

        return question.getString("question") + "\na: " + question.getString("a") + "\nb: " + question.getString("b") + "\nc: " + question.getString("c") + "\nd: " + question.getString("d" );
    }

    String getUserName(){
        return userName;
    }

    /*public int totalQuestionsl(){
      return correct + wrong;
    }*/

    private void newQuestion(){
        if(wrong + correct <= QuizQuestions.numberQuestions()){
            int myQ = selectQuestion.nextInt(QuizQuestions.numberQuestions()) + 1;

            while(usedQuestions.contains(myQ)){
                myQ = selectQuestion.nextInt(QuizQuestions.numberQuestions()) + 1;
            }

            currentQuestion = myQ;
        }else{
            currentQuestion = 0;
        }
    }

    int getCorrect(){
        return correct;
    }

    int getWrong(){
        return wrong;
    }


}
