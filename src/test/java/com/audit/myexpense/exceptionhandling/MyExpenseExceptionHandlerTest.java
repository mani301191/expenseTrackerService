package com.audit.myexpense.exceptionhandling;

import java.lang.reflect.Method;
import java.util.List;

import com.audit.myexpense.util.ExpenseConstant;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link MyExpenseExceptionHandler}.
 */
class MyExpenseExceptionHandlerTest {

    private final MyExpenseExceptionHandler handler = new MyExpenseExceptionHandler();

    private static MethodArgumentNotValidException validationException(BeanPropertyBindingResult bindingResult)
            throws NoSuchMethodException {
        Method endpoint = MyExpenseExceptionHandlerTest.class
                .getDeclaredMethod("sampleEndpoint", String.class);
        return new MethodArgumentNotValidException(new MethodParameter(endpoint, 0), bindingResult);
    }

    @SuppressWarnings("unused")
    private void sampleEndpoint(String value) {
        // only used to obtain a MethodParameter for the validation test
    }

    @Test
    void validationErrorsAreReportedAsBadRequestWithFieldMessages() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "expenseDetails");
        bindingResult.addError(new FieldError("expenseDetails", "amount", "must not be null"));
        bindingResult.addError(new FieldError("expenseDetails", "expenseType", "must not be blank"));

        ResponseEntity<List<String>> response =
                handler.processUnmergeException(validationException(bindingResult));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsExactly(
                "amount - must not be null",
                "expenseType - must not be blank");
    }

    @Test
    void duplicateKeyIsTranslatedToDuplicateRecordMessage() {
        ResponseEntity<String> response =
                handler.handleDuplicateKeyException(new DuplicateKeyException("E11000 duplicate key"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(ExpenseConstant.DUPLICATE_RECORD);
    }
}
