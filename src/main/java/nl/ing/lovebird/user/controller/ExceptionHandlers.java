package nl.ing.lovebird.user.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.errorhandling.ErrorDTO;
import nl.ing.lovebird.errorhandling.ErrorInfo;
import nl.ing.lovebird.errorhandling.ExceptionHandlingService;
import nl.ing.lovebird.user.exception.UserNotFoundException;
import org.slf4j.event.Level;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import static nl.ing.lovebird.user.controller.ErrorConstants.ILLEGAL_ARGUMENT;
import static nl.ing.lovebird.user.controller.ErrorConstants.USER_NOT_FOUND;

/**
 * Contains handlers for predefined exceptions.
 */
@ControllerAdvice
@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlers {

    private final ExceptionHandlingService service;

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({IllegalArgumentException.class})
    @ResponseBody
    public ErrorDTO handle(final IllegalArgumentException ex) {
        return logAndConstructWarning(ILLEGAL_ARGUMENT, ex);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({UserNotFoundException.class})
    @ResponseBody
    public ErrorDTO handle(final UserNotFoundException ex) {
        return logAndConstructWarning(USER_NOT_FOUND, ex);
    }

    private ErrorDTO logAndConstructWarning(ErrorInfo error, Throwable t) {
        return service.logAndConstruct(Level.WARN, error, t);
    }
}
