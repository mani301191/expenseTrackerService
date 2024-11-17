/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Manikandan Narasimhan
 *
 */
public class ExpenseCommonUtil {

	static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	public static String formattedDate(Date inputDate) {
		if (inputDate != null) {
				return dateFormat.format(inputDate);
		}
		return null;
	}
}
