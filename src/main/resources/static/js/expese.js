/* Copyright (C) 2021 Manikandan Narasimhan - All Rights Reserved
 * You may use, distribute and modify this code.
 */
$(document).ready(loadExpenseDetails(''));
	
				function loadExpenseDetails(url) {
					var serviceUrl= null;
					var currentDay = new Date();
					var onInit= true;
					if(url){
						serviceUrl = url;
						onInit= false;
					} else {
						serviceUrl= '/api/expenseTracker/getExpenseDetails'+'?year='+currentDay.getFullYear()
								+'&month='+getMonths(currentDay);
					}
							$.ajax({
										dataType : "json",
										cache : false,
										headers : {
											Accept : "application/json",
											"Access-Control-Allow-Origin" : "*"
										},
										type : "GET",
										url : serviceUrl,
										success : function(data) {
											 $('#tBody').empty();
											var trHTML = '';
											var totalExpense = 0;
											for (i = 0; i < data.length; i++) {
												trHTML += '<tr><td>'
														+ data[i].expenseId
														+ '</td><td>'
														+ data[i].year
														+ '</td><td>'
														+ data[i].month
														+ '</td><td>'
														+ data[i].expenseDate
														+ '</td><td>'
														+ data[i].expenseType
														+ '</td><td>'
														+ data[i].plannedExpense
														+ '</td><td>'
														+ data[i].description
														+ '</td><td>'
														+ data[i].amount
														+ '</td><td>'
														+'<button class="btn center noprint" onclick="deleteExpense('
														+data[i].expenseId+')">'
														+'<i class="fa fa-trash expdelete">'
														+'</i></button>'
														+ '</td></tr>';
												totalExpense = totalExpense
														+ data[i].amount;
											}
											trHTML += '<tr><td></td><td></td><td></td><td></td><td></td><td></td><td>'
													+ '<B>Total Expense : </B>'
													+ '</td><td>'
													+ totalExpense
													+ '</td></tr>'
											$('#tBody').append(trHTML);
											if(onInit) {
											  onloadCurrentDate(currentDay);
													}
										},
										error : function(data) {
										console.log(data);

										}
									});
							 
						}

		function createExpense() {
			
			var expenseDate = document.getElementById('expenseDate').value;
			var expMonth=null;
			var expYear=null;
			var dateObj = null;
			if(expenseDate && expenseDate != null) {
				dateObj = new Date(expenseDate);
				expMonth = getMonths(dateObj);
				expYear = getYear(dateObj);
			}
			var expenseType = document.getElementById('expenseType').value;
			var plannedExpense = document.getElementById('plannedExpense').value;
			var description = document.getElementById('description').value;
			var expenseAmount = document.getElementById('expenseAmount').value;
           if(expenseDate && expenseType && plannedExpense && description && expenseAmount) {
        	   var reqObj = {
        		        "year": expYear,
        		        "month": expMonth,
        		        "expenseDate": expenseDate,
        		        "amount": expenseAmount,
        		        "plannedExpense": plannedExpense,
        		        "description": description,
        		        "expenseType":expenseType
        	   }
        	   $.ajax({
       	        url: '/api/expenseTracker/saveExpenseDetails',
       	        method: 'POST',
       	        data: JSON.stringify(reqObj),
       	        dataType: 'json',
       	        contentType: 'application/json',
       	         success: function(result){
       	          resetCreateValues();
       	           $('#createstatus').text("Expense entry created successfully. Expense id : "+result.expenseId);
        	        setTimeout(function() {
                        $('#createstatus').empty();
                    }, 2000);
        	       console.log( "current val "+document.getElementById('month').value);
        	       document.getElementById('month').value=  dateObj.getFullYear()+'-'+(dateObj.getMonth()+1).toString().padStart(2, '0');
        	        document.getElementById('plannedExpenseSearch').value= plannedExpense;
        	        searchExpense();
       	         },
       	         error(jqXHR,textStatus, errorThrown){
       	         console.log(jqXHR);
       	         console.log(textStatus);
       	         console.log(errorThrown);
       	         }
       	    });
           } else {
        	   validatCreateField(document.getElementById('expenseDate'),expenseDate);
        	   validatCreateField(document.getElementById('expenseType'), expenseType);
        	   validatCreateField(document.getElementById('plannedExpense'), plannedExpense);
        	   validatCreateField(document.getElementById('description'), description);
        	   validatCreateField(document.getElementById('expenseAmount'), expenseAmount);  
           }
		}
		
		function validatCreateField(field,value) {
			if(!value) {
				field.classList.add("empty");
			}else {
				field.classList.remove("empty");
			}
		}
		
		function getMonths(date) {
			if(date) {
			var  months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
			return months[date.getMonth()];
			}
		}
		
		function getYear(date){
			if(date) {
			return date.getFullYear();
			}
		}
		function resetCreateValues() {
			document.getElementById('expenseDate').value= '';
			document.getElementById('expenseType').value= '';
			document.getElementById('plannedExpense').value= '';
			document.getElementById('description').value= '';
			document.getElementById('expenseAmount').value= '';
		}
		
		function searchExpense() {
			var selectedDate = document.getElementById('month').value;
			var plannedExpense = document.getElementById('plannedExpenseSearch').value;
			var dateObj = null;
			var url = null;
			if(selectedDate) {
				dateObj = new Date(selectedDate);
				url = '/api/expenseTracker/getExpenseDetails'+'?year='+dateObj.getFullYear()
				+'&month='+getMonths(dateObj);
				if(plannedExpense) {
					url= url+'&plannedExpense='+plannedExpense;
				}
				loadExpenseDetails(url);
			} 
			$('#expMonth').text(' ('+getMonths(dateObj) + ' - '+ getYear(dateObj)+ ')');
		}
		
		function onloadCurrentDate(dateObj) {
			document.getElementById('month').value=  dateObj.getFullYear()+'-'+(dateObj.getMonth()+1).toString().padStart(2, '0');
			$('#expMonth').text(' ('+getMonths(dateObj) + ' - '+ getYear(dateObj)+ ')');
		}
		
		function deleteExpense(id) {
			 $.ajax({
	       	        url: '/api/expenseTracker/expenseDetail/'+id,
	       	        method: 'delete',
	       	        contentType: 'application/json',
	       	         success: function(){
	       	           $('#deletestatus').text('Expense id '+id+' - Deleted successfully ');
	       	           searchExpense() ;
	       	           setTimeout(function() {
	                        $('#deletestatus').empty();
	                    }, 2000);
	       	         },
	       	         error(jqXHR,textStatus, errorThrown){
	       	         console.log(jqXHR);
	       	         console.log(textStatus);
	       	         console.log(errorThrown);
	       	         }
	       	    });
		}
		
		function printDiv(divName){
			// getting only the required div for print
			var printContents = document.getElementById(divName).innerHTML;
			var tableContents = document.getElementById('expensetable').innerHTML;
			// getting the original dom
			var originalContents = document.body.innerHTML;
			var selectedMonth = document.getElementById('month').value;
			document.body.innerHTML = printContents + tableContents;
			// window print
			window.print();
			// again setting the orginal dom
			document.body.innerHTML = originalContents;
			document.getElementById('month').value= selectedMonth;

		}