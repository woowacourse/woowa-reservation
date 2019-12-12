package com.h3.reservation.exceptionhandler;

import com.h3.reservation.calendar.domain.exception.InvalidDateTimeRangeException;
import com.h3.reservation.slack.dto.response.ModalErrorResponse;
import com.h3.reservation.slack.fragment.error.DateTimeErrors;
import com.h3.reservation.slack.fragment.error.TimeErrors;
import com.h3.reservation.slackcalendar.exception.InvalidTimeRangeException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author heebg
 * @version 1.0
 * @date 2019-12-12
 */
@ControllerAdvice(basePackages = {"com.h3.reservation.controller"})
public class GlobalExceptionHandler {
    @ResponseBody
    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<ModalErrorResponse> handleInvalidTimeRangeException(InvalidTimeRangeException e) {
        return ResponseEntity.ok(new ModalErrorResponse(new TimeErrors(e.getMessage())));
    }

    @ResponseBody
    @ExceptionHandler(InvalidDateTimeRangeException.class)
    public ResponseEntity handleInvalidDateTimeRangeException(InvalidDateTimeRangeException e) {
        return ResponseEntity.ok(new ModalErrorResponse(new DateTimeErrors(e.getMessage())));
    }
}