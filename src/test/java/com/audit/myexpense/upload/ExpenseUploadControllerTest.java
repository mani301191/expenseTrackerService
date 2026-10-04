package com.audit.myexpense.upload;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.IncomeDetails;
import com.audit.myexpense.model.MonthlyTarget;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExpenseUploadController}.
 */
@ExtendWith(MockitoExtension.class)
class ExpenseUploadControllerTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ExpenseUploadController controller;

    private static MockMultipartFile statement(String... rows) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Statement");
            Row header = sheet.createRow(0);
            String[] headers = {"Value Date", "Transaction Date", "Cheque", "Description", "Ref", "Amount", "Dr/Cr"};
            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }
            int rowIndex = 1;
            for (String row : rows) {
                String[] columns = row.split("\\|", -1);
                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(1).setCellValue(columns[0]);
                dataRow.createCell(3).setCellValue(columns[1]);
                dataRow.createCell(5).setCellValue(columns[2]);
                dataRow.createCell(6).setCellValue(columns[3]);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            workbook.write(output);
            return new MockMultipartFile("file", "statement.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", output.toByteArray());
        }
    }

    private static MonthlyTarget target(String description) {
        MonthlyTarget target = new MonthlyTarget();
        target.description = description;
        target.amount = 100.0;
        target.year = 2024;
        target.month = "January";
        return target;
    }

    @Test
    void emptyFileIsRejectedWithoutTouchingDatabase() throws Exception {
        MockMultipartFile emptyFile = new MockMultipartFile("file", "statement.xlsx", "application/octet-stream",
                new byte[0]);

        ResponseEntity<String> response = controller.uploadStatement(emptyFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).contains("No file uploaded or file is empty");
        verifyNoInteractions(mongoTemplate);
    }

    @Test
    void debitRowsBecomeExpensesAndCreditRowsBecomeIncome() throws Exception {
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(IncomeDetails.class))).thenReturn(null);
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Collections.singletonList(target("Groceries")));
        when(mongoTemplate.insert(any(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mongoTemplate.insert(any(IncomeDetails.class), eq("myIncomeDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = statement(
                "15-01-2024 10:30|GROCERIES MART PURCHASE|1,000.50|DR",
                "31-01-2024 17:45|SALARY CREDIT JANUARY|50,000.00|CR");

        ResponseEntity<String> response = controller.uploadStatement(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        ArgumentCaptor<ExpenseDetails> expenseCaptor = ArgumentCaptor.forClass(ExpenseDetails.class);
        verify(mongoTemplate).insert(expenseCaptor.capture(), eq("myExpenseDetail"));
        ExpenseDetails expense = expenseCaptor.getValue();
        assertThat(expense.expenseId).isZero();
        assertThat(expense.expenseDate).isNotNull();
        assertThat(expense.amount).isEqualTo(1000.50);
        assertThat(expense.expenseOf).isEqualTo("Groceries");
        assertThat(expense.expenseType).isEqualTo("Planned");
        assertThat(expense.updatedDate).isNotBlank();

        ArgumentCaptor<IncomeDetails> incomeCaptor = ArgumentCaptor.forClass(IncomeDetails.class);
        verify(mongoTemplate).insert(incomeCaptor.capture(), eq("myIncomeDetail"));
        IncomeDetails income = incomeCaptor.getValue();
        assertThat(income.getIncomeId()).isZero();
        assertThat(income.getAmount()).isEqualTo(50000.0);
        assertThat(income.getSource()).isEqualTo("SALARY CREDIT JANUARY");
        assertThat(income.getUpdatedDate()).isNotBlank();
    }

    @Test
    void unmatchedTransactionsAreFlaggedUnplanned() throws Exception {
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(null);
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Collections.singletonList(target("Groceries")));
        when(mongoTemplate.insert(any(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = statement("15-01-2024 10:30|RANDOM TRANSFER 999|250.00|DR");
        controller.uploadStatement(file);

        ArgumentCaptor<ExpenseDetails> captor = ArgumentCaptor.forClass(ExpenseDetails.class);
        verify(mongoTemplate).insert(captor.capture(), eq("myExpenseDetail"));
        assertThat(captor.getValue().expenseOf).isEqualTo("Uncategorized");
        assertThat(captor.getValue().expenseType).isEqualTo("UnPlanned");
    }

    @Test
    void investmentKeywordsAreClassifiedAsInvestment() throws Exception {
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(null);
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Collections.singletonList(target("NPS")));
        when(mongoTemplate.insert(any(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = statement("15-01-2024 10:30|NPS CONTRIBUTION JAN|5000.00|DR");
        controller.uploadStatement(file);

        ArgumentCaptor<ExpenseDetails> captor = ArgumentCaptor.forClass(ExpenseDetails.class);
        verify(mongoTemplate).insert(captor.capture(), eq("myExpenseDetail"));
        assertThat(captor.getValue().expenseOf).isEqualTo("NPS");
        assertThat(captor.getValue().expenseType).isEqualTo("Investment");
    }

    @Test
    void duplicateExpensesDoNotAbortProcessing() throws Exception {
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(IncomeDetails.class))).thenReturn(null);
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Collections.emptyList());
        when(mongoTemplate.insert(any(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenThrow(new DuplicateKeyException("dup"));
        when(mongoTemplate.insert(any(IncomeDetails.class), eq("myIncomeDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MockMultipartFile file = statement(
                "15-01-2024 10:30|DUPLICATED PURCHASE|10.00|DR",
                "16-01-2024 09:00|REFUND CREDIT|5.00|CR");

        ResponseEntity<String> response = controller.uploadStatement(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mongoTemplate).insert(any(IncomeDetails.class), eq("myIncomeDetail"));
    }

    @Test
    void rowsWithUnparseableDatesAreSkipped() throws Exception {
        MockMultipartFile file = statement("not-a-date|RANDOM PURCHASE|10.00|DR");

        ResponseEntity<String> response = controller.uploadStatement(file);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mongoTemplate, never()).insert(any(ExpenseDetails.class), eq("myExpenseDetail"));
        verify(mongoTemplate, never()).insert(any(IncomeDetails.class), eq("myIncomeDetail"));
    }

    @Test
    void processingFailuresYieldServerErrorResponse() throws Exception {
        MultipartFile brokenFile = org.mockito.Mockito.mock(MultipartFile.class);
        when(brokenFile.isEmpty()).thenReturn(false);
        when(brokenFile.getInputStream()).thenThrow(new IOException("corrupted stream"));

        ResponseEntity<String> response = controller.uploadStatement(brokenFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("Error processing statement:").contains("corrupted stream");
    }

    private static MockMultipartFile csv() throws IOException {
        String content = "Sr No,Transaction Date,Value Date,Description,Cheque No,Amount,Dr/Cr,Balance\n" +
                "1,15-01-2024 10:30:00,15-01-2024 10:30:00,GROCERIES MART PURCHASE,12345,\"1,000.50\",DR,5000.00\n" +
                "2,31-01-2024 17:45:00,31-01-2024 17:45:00,SALARY CREDIT JANUARY,0,\"50,000.00\",CR,55000.00\n";
        return new MockMultipartFile("file", "KM8137398_statement (3).csv",
                "text/csv", content.getBytes());
    }

    @Test
    void csvFileProcessesCorrectly() throws Exception {
        when(mongoTemplate.findOne(any(Query.class), eq(ExpenseDetails.class))).thenReturn(null);
        when(mongoTemplate.findOne(any(Query.class), eq(IncomeDetails.class))).thenReturn(null);
        when(mongoTemplate.find(any(Query.class), eq(MonthlyTarget.class), eq("myMonthlyTarget")))
                .thenReturn(Collections.singletonList(target("Groceries")));
        when(mongoTemplate.insert(any(ExpenseDetails.class), eq("myExpenseDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(mongoTemplate.insert(any(IncomeDetails.class), eq("myIncomeDetail")))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = controller.uploadStatement(csv());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(mongoTemplate).insert(any(ExpenseDetails.class), eq("myExpenseDetail"));
        verify(mongoTemplate).insert(any(IncomeDetails.class), eq("myIncomeDetail"));
    }
}
