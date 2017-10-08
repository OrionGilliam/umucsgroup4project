package bot.schedule;

public class ScheduleException extends Exception {
    String failureReason;

    public ScheduleException(String cause) {
        failureReason = cause;
    }

    public String getFailureReason() {
        return failureReason;
    }

}
