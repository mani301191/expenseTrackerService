package com.audit.myexpense.upload;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.IncomeDetails;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/expenseTracker")
public class ExpenseUploadController {

    private final MongoTemplate mongoTemplate;

    public ExpenseUploadController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Upload Bank Statement CSV (HDFC/Kotak)
     */
    @PostMapping("/uploadStatement")
    public ResponseEntity<String> uploadStatement(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
        }
        try {
            processAccountStatement(file.getInputStream());

            return ResponseEntity.ok("Statement uploaded and processed successfully!");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing statement: " + e.getMessage());
        }
    }

    private void processAccountStatement(InputStream excelInputStream) throws Exception {
        Workbook workbook = new XSSFWorkbook(excelInputStream);
        Sheet sheet = workbook.getSheetAt(0);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

        for (int i = 1; i <= sheet.getLastRowNum(); i++) { // skip header
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // Read columns based on structure
            String transactionDateStr = getCellValueAsString(row.getCell(1)); // Transaction Date
            String description = getCellValueAsString(row.getCell(3));        // Description
            String amountStr = getCellValueAsString(row.getCell(5));          // Amount
            String drCr = getCellValueAsString(row.getCell(6)).toUpperCase(); // DR / CR

            if (transactionDateStr.isEmpty() || amountStr.isEmpty()) continue;

            // Parse date
            LocalDate txnDate;
            try {
                txnDate = LocalDateTime.parse(transactionDateStr, dateFormatter).toLocalDate();
            } catch (DateTimeParseException e) {
                System.err.println("Date parse failed for: " + transactionDateStr);
                continue;
            }

            // Clean amount (handle commas)
            double amount = Double.parseDouble(amountStr.replace(",", "").trim());

            if (drCr.equals("DR")) {
                ExpenseDetails exp = new ExpenseDetails();
                Query query = new Query();
                query.with( Sort.by(Sort.Direction.DESC, "expenseId"));
                query.limit(1);
                ExpenseDetails maxObject = mongoTemplate.findOne(query, ExpenseDetails.class);
                exp.expenseId= maxObject!= null ?maxObject.expenseId+1:0;
                Date date = Date.from(txnDate.atStartOfDay(ZoneOffset.UTC).toInstant());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                exp.month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);
                exp.year = cal.get(Calendar.YEAR);
                exp.expenseDate = date;
                exp.amount = amount;
                exp.expenseOf = "Uncategorized";
                exp.description = description;
                exp.expenseType = "UnPlanned";
                exp.updatedDate = ExpenseCommonUtil.formattedDate(new Date());
            try {
                mongoTemplate.insert(exp, "myExpenseDetail");
            } catch (DuplicateKeyException e) {
                // Handle the duplicate entry
                System.err.println("Duplicate expense entry: " + e.getMessage());
            }
            } else if (drCr.equals("CR")) {
                IncomeDetails inc = new IncomeDetails();
                Query query = new Query();
                query.with( Sort.by(Sort.Direction.DESC, "incomeId"));
                query.limit(1);
                IncomeDetails maxObject = mongoTemplate.findOne(query, IncomeDetails.class);
                inc.setIncomeId(maxObject!= null ?maxObject.getIncomeId()+1:0);
                Date date = Date.from(txnDate.atStartOfDay(ZoneOffset.UTC).toInstant());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                inc.setYear(cal.get(Calendar.YEAR));
                inc.setMonth( cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH));
                inc.setIncomeDate(date);
                inc.setAmount(amount);
                inc.setSource(description);
                inc.setUpdatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                try {
                mongoTemplate.insert(inc, "myIncomeDetail");
                } catch (DuplicateKeyException e) {
                    // Handle the duplicate entry
                    System.err.println("Duplicate expense entry: " + e.getMessage());
                }
            }
        }

        workbook.close();
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                    return sdf.format(cell.getDateCellValue());
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                return cell.getCellFormula();

            default:
                return "";
        }
    }


}
