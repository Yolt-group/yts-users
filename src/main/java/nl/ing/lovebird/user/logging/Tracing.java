package nl.ing.lovebird.user.logging;

import brave.baggage.BaggageField;
import lombok.experimental.UtilityClass;
import nl.ing.lovebird.logging.MDCContextCreator;
import org.slf4j.MDC;

import java.util.UUID;

@UtilityClass
public class Tracing {

    public static final BaggageField USER_ID_TRACE = BaggageField.create(MDCContextCreator.USER_ID_HEADER_NAME);

}
