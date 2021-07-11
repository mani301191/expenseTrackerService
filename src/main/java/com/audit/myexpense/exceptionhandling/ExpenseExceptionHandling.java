/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.exceptionhandling;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.mongodb.MongoWriteException;

/**
 * @author Manikandan Narasimhan
 *
 */
@ControllerAdvice
public class ExpenseExceptionHandling extends ResponseEntityExceptionHandler {

	@ExceptionHandler(MongoWriteException.class)
	public ResponseEntity<Object> handleMongoWriteException(MongoWriteException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("status", "Exception occured");
		body.put("message", ex.getMessage());
		body.put("timestamp", LocalDateTime.now());
		return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	}

	// @Override
	// protected ResponseEntity<Object> handleMethodArgumentNotValid(
	// MethodArgumentNotValidException ex, HttpHeaders headers,
	// HttpStatus status, WebRequest request) {
	//
	// Map<String, Object> body = new LinkedHashMap<>();
	// body.put("timestamp", LocalDate.now());
	// body.put("status", status.value());
	//
	// List<String> errors = ex.getBindingResult()
	// .getFieldErrors()
	// .stream()
	// .map(x -> x.getDefaultMessage())
	// .collect(Collectors.toList());
	//
	// body.put("errors", errors);
	//
	// return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
	// }
}
