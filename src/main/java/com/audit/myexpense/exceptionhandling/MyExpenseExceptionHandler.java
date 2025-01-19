/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
package com.audit.myexpense.exceptionhandling;

import static com.audit.myexpense.util.ExpenseConstant.DUPLICATE_RECORD;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Manikandan Narasimhan
 *
 */
@ControllerAdvice
public class MyExpenseExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(MyExpenseExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<List<String>> processUnmergeException(final
                                                                MethodArgumentNotValidException ex) {

        List<String> list = ex.getBindingResult().getFieldErrors().stream()
                .map( a-> a.getField()+" - " +a.getDefaultMessage() )
                .collect(Collectors.toList());
        return new ResponseEntity<>(list, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    @ResponseBody
    public ResponseEntity<String> handleDuplicateKeyException(final DuplicateKeyException ex) {
        logger.error(ex.getMessage());
        return new ResponseEntity<>(DUPLICATE_RECORD, HttpStatus.BAD_REQUEST);
    }
}
