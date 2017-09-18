package bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.Random;

@SpringBootApplication(scanBasePackages = {"me.ramswaroop.jbot", "bot"})
public class UmucSlackBotApplication {

    /**
     * Entry point of the application. Run this method to start the sample bots,
     * but don't forget to add the correct tokens in application.properties file.
     *
     * @param args
     */
    public static void main(String[] args) {
        File file = new File(ClassLoader.getSystemResource("quiz.json").getFile());
        SpringApplication.run(UmucSlackBotApplication.class, args);
    }
}
