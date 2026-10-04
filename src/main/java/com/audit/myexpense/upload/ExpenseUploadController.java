package com.audit.myexpense.upload;

import com.audit.myexpense.model.ExpenseDetails;
import com.audit.myexpense.model.IncomeDetails;
import com.audit.myexpense.model.MonthlyTarget;
import com.audit.myexpense.util.ExpenseCommonUtil;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;


@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/expenseTracker")
public class ExpenseUploadController {

    private static final DateTimeFormatter[] DATE_FORMATTERS = new DateTimeFormatter[]{
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"),
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
    };
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
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null) {
                originalFilename = "";
            }
            String lowerCaseFilename = originalFilename.toLowerCase();


            if (lowerCaseFilename.endsWith(".csv")) {
                processCsvStatement(file.getInputStream());
            } else {
                processAccountStatement(file.getInputStream(), lowerCaseFilename);
            }

            return ResponseEntity.ok("Statement uploaded and processed successfully!");

        } catch (Exception e) {
            System.out.println("Error processing statement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing statement: " + e.getMessage());
        }
    }

    private void processCsvStatement(InputStream inputStream) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        int lineNumber = 0;

        while ((line = br.readLine()) != null) {
            lineNumber++;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            String lowerTrimmed = trimmed.toLowerCase();
            // Skip header lines - look for lines that contain transaction data
            if (lineNumber == 1 && lowerTrimmed.contains("account statement")) {
                continue;
            }
            if (lowerTrimmed.contains("sl. no.") || lowerTrimmed.contains("transaction date") || lowerTrimmed.contains("transaction type")) {
                // Skip header row
                continue;
            }
            if (lowerTrimmed.contains("closing balance") || lowerTrimmed.contains("important note") || lowerTrimmed.contains("reward") && lowerTrimmed.contains("summary")) {
                // Don't break for all summary lines in HDFC CSV - but avoid processing them
            }
            if (trimmed.contains("~|~")) {
                // HDFC CSV - skip non-transaction rows that don't have amounts in expected position
                String[] f = trimmed.split("~\\|~", -1);
                if (f.length < 5) continue;
                // Skip summary rows
                if (f[0].toLowerCase().contains("account summary") || f[0].toLowerCase().contains("total") && f[0].toLowerCase().contains("amount")) {
                    // Skip summary lines
                    continue;
                }
            } else {
                if (lowerTrimmed.contains("closing balance") || lowerTrimmed.contains("important note")) {
                    break;
                }
            }
            // Split by comma/pipe, handling quoted fields
            String delimiter = ",";
            if (trimmed.contains("~|~")) {
                delimiter = "~\\|~";
            }
            String[] fields;
            if (trimmed.contains("~|~")) {
                fields = trimmed.split(delimiter, -1);
            } else {
                fields = splitCsvLine(trimmed);
            }
            if (fields.length < 4) {
                continue;
            }
            // Try to identify column positions
            String transactionDateStr = null;
            String description = null;
            String amountStr = null;
            String drCr = "";
            if (trimmed.contains("~|~")) {
                // HDFC style CSV with ~|~ delimiter
                // Header: Transaction type~|~Primary/Add...~|~DATE~|~Description~|~AMT~|~Debit/Credit~
                for (int i = 0; i < fields.length; i++) {
                    String f = fields[i].replaceAll("^\"|\"$", "").trim().toLowerCase();
                    if (f.equals("date") || f.contains("date") && f.contains("time")) {
                        // next in same row? no, look at positions from header row better; but we may not have header in this line
                    }
                }
                // For data rows, positions: 2 is DATE, 3 is Description, 4 is AMT, 5 is Debit/Credit
                if (fields.length > 5) {
                    transactionDateStr = fields[2].replaceAll("^\"|\"$", "");
                    description = fields[3].replaceAll("^\"|\"$", "");
                    amountStr = fields[4].replaceAll("^\"|\"$", "");
                    if (fields.length > 5) {
                        drCr = fields[5].replaceAll("^\"|\"$", "").toUpperCase();
                    }
                }
            } else {
                // Kotak style CSV
                transactionDateStr = fields[1].replaceAll("^\"|\"$", "");
                description = fields[3].replaceAll("^\"|\"$", "");
                amountStr = fields[5].replaceAll("^\"|\"$", "");
                if (fields.length > 6) {
                    drCr = fields[6].replaceAll("^\"|\"$", "").toUpperCase();
                }
            }

            if (transactionDateStr.isEmpty() || amountStr.isEmpty() || !amountStr.matches(".*[0-9].*")) {
                continue;
            }

            LocalDate txnDate = null;
            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    if (transactionDateStr.contains(":")) {
                        txnDate = LocalDateTime.parse(transactionDateStr.trim(), formatter).toLocalDate();
                    } else {
                        txnDate = LocalDate.parse(transactionDateStr.trim(), formatter);
                    }
                    break;
                } catch (DateTimeParseException e) {
                    System.out.println("Date parse failed: " + transactionDateStr);
                }
            }
            if (txnDate == null) {
                // Try alternative formats like dd/MM/yyyy
                try {
                    if (transactionDateStr.contains("/")) {
                        String[] parts = transactionDateStr.split("/");
                        if (parts.length == 3) {
                            String clean = parts[0].trim() + "/" + parts[1].trim() + "/" + parts[2].trim().split(" ")[0];
                            txnDate = LocalDate.parse(clean, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        }
                    }
                    } catch (Exception ex) {
                        System.out.println("Date parse failed: " + transactionDateStr);
                    }
            }
            if (txnDate == null) {

                continue;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr.replace(",", "").replace(" ", "").trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid amount format: " + amountStr);
                continue;
            }

            if (drCr.equals("DR") || (drCr.isEmpty() && line != null && line.contains("~|~"))) {
                ExpenseDetails exp = new ExpenseDetails();
                Query query = new Query();
                query.with(Sort.by(Sort.Direction.DESC, "expenseId"));
                query.limit(1);
                ExpenseDetails maxObject = mongoTemplate.findOne(query, ExpenseDetails.class);
                exp.expenseId = maxObject != null ? maxObject.expenseId + 1 : 0;
                Date date = Date.from(txnDate.atStartOfDay(ZoneOffset.UTC).toInstant());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                exp.month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);
                exp.year = cal.get(Calendar.YEAR);
                exp.expenseDate = date;
                exp.amount = amount;
                exp.expenseOf = findMatchingExpenseOf(description, exp.year, exp.month);
                exp.description = description;
                exp.expenseType = determineExpenseType(exp.expenseOf);
                exp.updatedDate = ExpenseCommonUtil.formattedDate(new Date());
                try {
                    mongoTemplate.insert(exp, "myExpenseDetail");
                } catch (DuplicateKeyException ex) {
                    System.err.println("Duplicate expense entry: " + ex.getMessage());
                }
            } else if (drCr.equals("CR")) {
                IncomeDetails inc = new IncomeDetails();
                Query query = new Query();
                query.with(Sort.by(Sort.Direction.DESC, "incomeId"));
                query.limit(1);
                IncomeDetails maxObject = mongoTemplate.findOne(query, IncomeDetails.class);
                inc.setIncomeId(maxObject != null ? maxObject.getIncomeId() + 1 : 0);
                Date date = Date.from(txnDate.atStartOfDay(ZoneOffset.UTC).toInstant());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                inc.setYear(cal.get(Calendar.YEAR));
                inc.setMonth(cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH));
                inc.setIncomeDate(date);
                inc.setAmount(amount);
                inc.setSource(description);
                inc.setUpdatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                try {
                    mongoTemplate.insert(inc, "myIncomeDetail");
                } catch (DuplicateKeyException ex) {
                    System.err.println("Duplicate income entry: " + ex.getMessage());
                }
            }
        }
        br.close();
    }

    private String[] splitCsvLine(String line) {
        List<String> result = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    private void processAccountStatement(InputStream excelInputStream, String filename) throws Exception {
        Workbook workbook;
        if (filename.endsWith(".xls")) {
            workbook = new HSSFWorkbook(excelInputStream);
        } else {
            workbook = new XSSFWorkbook(excelInputStream);
        }
        try {
            Sheet sheet = workbook.getSheetAt(0);



            // Detect column positions by scanning header
            int startRow = 1;
            int dateCol = 1; // default for original format
            int descCol = 3;
            int amountCol = 5;
            int drCrCol = 6;

            // Try to detect header row
            for (int r = 0; r <= Math.min(10, sheet.getLastRowNum()); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                StringBuilder rowContent = new StringBuilder();
                for (int c = 0; c < Math.min(10, row.getLastCellNum()); c++) {
                    rowContent.append(getCellValueAsString(row.getCell(c)).toLowerCase()).append(" ");
                }
                String content = rowContent.toString();
                if (content.contains("transaction date") || content.contains("txn date") || content.contains("date")) {
                    // try to map columns
                    for (int c = 0; c < Math.min(10, row.getLastCellNum()); c++) {
                        String val = getCellValueAsString(row.getCell(c)).toLowerCase();
                        if (val.contains("transaction") && val.contains("date")) dateCol = c;
                        else if (val.contains("description") || val.contains("narration") || val.contains("particulars") || val.contains("details"))
                            descCol = c;
                        else if (val.contains("amount")) amountCol = c;
                        else if (val.contains("dr") || val.contains("cr") || val.contains("debit") || val.contains("credit"))
                            drCrCol = c;
                    }
                    startRow = r + 1;
                    break;
                }
            }

            for (int i = startRow; i <= sheet.getLastRowNum(); i++) { // skip header
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // Read columns based on detected structure
                String transactionDateStr = getCellValueAsString(row.getCell(dateCol));
                String description = getCellValueAsString(row.getCell(descCol));
                String amountStr = getCellValueAsString(row.getCell(amountCol));
                String drCr = getCellValueAsString(row.getCell(drCrCol)).toUpperCase();

                if (transactionDateStr.isEmpty() || amountStr.isEmpty() || !amountStr.matches(".*[0-9].*")) {
                    continue;
                }

                // Parse date
                LocalDate txnDate = null;
                for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                    try {
                        if (transactionDateStr.contains(":")) {
                            txnDate = LocalDateTime.parse(transactionDateStr, formatter).toLocalDate();
                        } else {
                            txnDate = LocalDate.parse(transactionDateStr, formatter);
                        }
                        break;
                    } catch (DateTimeParseException e) {
                        System.err.println("Date parse failed for: " + transactionDateStr);
                    }
                }
                if (txnDate == null) {
    
                    continue;
                }

                // Clean amount (handle commas)
                double amount;
                try {
                    amount = Double.parseDouble(amountStr.replace(",", "").trim());
                } catch (NumberFormatException e) {
                    continue;
                }

            if (drCr.equals("DR") || drCr.contains("DEBIT") || drCr.isEmpty() || !drCr.equals("CR")) {
                ExpenseDetails exp = new ExpenseDetails();
                Query query = new Query();
                query.with(Sort.by(Sort.Direction.DESC, "expenseId"));
                query.limit(1);
                ExpenseDetails maxObject = mongoTemplate.findOne(query, ExpenseDetails.class);
                exp.expenseId = maxObject != null ? maxObject.expenseId + 1 : 0;
                Date date = Date.from(txnDate.atStartOfDay(ZoneOffset.UTC).toInstant());
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                exp.month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH);
                exp.year = cal.get(Calendar.YEAR);
                exp.expenseDate = date;
                exp.amount = amount;
                exp.expenseOf = findMatchingExpenseOf(description, exp.year, exp.month);
                exp.description = description;
                exp.expenseType = determineExpenseType(exp.expenseOf);
                exp.updatedDate = ExpenseCommonUtil.formattedDate(new Date());
                try {
                    mongoTemplate.insert(exp, "myExpenseDetail");
                } catch (DuplicateKeyException ex) {
                    System.err.println("Duplicate expense entry: " + ex.getMessage());
                }
            } else if (drCr.equals("CR") || drCr.contains("CREDIT")) {
                    IncomeDetails inc = new IncomeDetails();
                    Query query = new Query();
                    query.with(Sort.by(Sort.Direction.DESC, "incomeId"));
                    query.limit(1);
                    IncomeDetails maxObject = mongoTemplate.findOne(query, IncomeDetails.class);
                    inc.setIncomeId(maxObject != null ? maxObject.getIncomeId() + 1 : 0);
                    Date date = Date.from(txnDate.atStartOfDay(ZoneOffset.UTC).toInstant());
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(date);
                    inc.setYear(cal.get(Calendar.YEAR));
                    inc.setMonth(cal.getDisplayName(Calendar.MONTH, Calendar.LONG_FORMAT, Locale.ENGLISH));
                    inc.setIncomeDate(date);
                    inc.setAmount(amount);
                    inc.setSource(description);
                    inc.setUpdatedDate(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    try {
                        mongoTemplate.insert(inc, "myIncomeDetail");
                    } catch (DuplicateKeyException ex) {
                        System.out.println("Duplicate income entry skipped");
                    }
                }
            }
        } finally {
            workbook.close();
        }
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


    private String findMatchingExpenseOf(String txnDescription, int year, String month) {
        Query targetQuery = new Query();
        targetQuery.addCriteria(Criteria.where("year").is(year).and("month").is(month));
        List<MonthlyTarget> targets = mongoTemplate.find(targetQuery, MonthlyTarget.class, "myMonthlyTarget");

        if (targets == null || targets.isEmpty()) {
            return "Uncategorized";
        }

        String normalizedTxn = txnDescription.toLowerCase();

        // Simple contains check (can be replaced with fuzzy)
        for (MonthlyTarget t : targets) {
            if (normalizedTxn.contains(t.description.toLowerCase())) {
                return t.description;
            }
        }

        return "Uncategorized";
    }

    private String determineExpenseType(String expenseOf) {
        if ("Uncategorized".equals(expenseOf)) {
            return "UnPlanned";
        }
        String normalized = expenseOf.toLowerCase();
        // investmentKeywords should be updated as configurable
        List<String> investmentKeywords = Arrays.asList("nps", "ssa", "zerodha", "coin", "sip", "mutual fund");
        for (String keyword : investmentKeywords) {
            if (normalized.contains(keyword)) {
                return "Investment";
            }
        }
        return "Planned";
    }

}
