package nl.ing.lovebird.user.kafka.producer;

public class PublishingEventFailedException extends RuntimeException {

    public PublishingEventFailedException(Throwable cause) {
        super(cause);
    }
}
