package bot.dieroller;

import java.util.Random;

public class DieRoller {

    private static Random rand = new Random();


    public static String rollByString(String rollMe){
        try{
        String[] splitdice = rollMe.split("[dD]");
        int numbDice = Integer.parseInt(splitdice[0]);
        int numbSides = Integer.parseInt(splitdice[1]);

        return rollDie(numbSides, numbDice);

    }catch(Exception e){
        return  "Dice format error";
    }
    }


    private static String rollDie(int numbSides, int numbDice){
        String returnResult = "Dice rolled: ";
        int total = 0;
        int[] results = new int[numbDice];


        for (int i = 0; i < results.length; i++){
            results[i] = rand.nextInt(numbSides) + 1;

            if(i ==0){
                returnResult += results[i];
            }
            else{
                returnResult += ", " + results[i];
            }
            total += results[i];
        }

        returnResult += ".  Total: " + total;

        return returnResult;
    }

}
