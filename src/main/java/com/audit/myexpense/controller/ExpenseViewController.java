/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */

package com.audit.myexpense.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author Manikandan Narasimhan
 *
 */
@Controller
public class ExpenseViewController {

	/**
	 * @param model
	 * @return myexpense
	 */
	@RequestMapping("/")
	public String myexpense(Model model) {
		return "myexpense";
	}
}
