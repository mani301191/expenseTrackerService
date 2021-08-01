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
											var plannedExp = 0;
											var unplannedExp = 0;
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
														+ '</td><td class="noprint">'
														+'<button class="btn center noprint" onclick="deleteExpense('
														+data[i].expenseId+')">'
														+'<i class="fa fa-trash expdelete">'
														+'</i></button>'
														+ '</td></tr>';
												totalExpense = totalExpense
														+ data[i].amount;
												if(data[i].plannedExpense === "Yes") {
												plannedExp = plannedExp + data[i].amount;
												} else {
												unplannedExp = unplannedExp + data[i].amount
												}
											}
											trHTML += '<tr  class="noprint" ><td></td><td></td><td></td><td></td><td></td><td></td><td>'
													+ '<B>Total Expense : </B>'
													+ '</td><td>'
													+ totalExpense
													+ '</td></tr>';
											$('#plannedExp').text(plannedExp);	
											$('#unplannedExp').text(unplannedExp);
											$('#totalExpense').text(totalExpense);
											$('#overAllExpense').text(totalExpense);
											$('#tBody').append(trHTML);
											if(onInit) {
											  onloadCurrentDate(currentDay);
											  searchIncome();
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
                    }, 5000);
        	       console.log( "current val "+document.getElementById('month').value);
        	       document.getElementById('month').value=  dateObj.getFullYear()+'-'+(dateObj.getMonth()+1).toString().padStart(2, '0');
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
		
		function searchIncome() {
			var selectedDate = document.getElementById('incomemonth').value;
			var dateObj = null;
			var serviceUrl = null;
			if(selectedDate) {
				dateObj = new Date(selectedDate);
				serviceUrl = '/api/expenseTracker/getIncomeDetails'+'?year='+dateObj.getFullYear()
				+'&month='+getMonths(dateObj);
			}  else{
				dateObj =  new Date();
				document.getElementById('incomemonth').value=  dateObj.getFullYear()+'-'+(dateObj.getMonth()+1).toString().padStart(2, '0');
				serviceUrl = '/api/expenseTracker/getIncomeDetails'+'?year='+dateObj.getFullYear()
				+'&month='+getMonths(dateObj);
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
					 $('#thIncomeBody').empty();
					var trHTML = '';
					var totalIncome = 0;
					var unplannedExp = 0;
					for (i = 0; i < data.length; i++) {
						trHTML += '<tr><td>'
								+ data[i].incomeId
								+ '</td><td>'
								+ data[i].year
								+ '</td><td>'
								+ data[i].month
								+ '</td><td>'
								+ data[i].incomeDate
								+ '</td><td>'
								+ data[i].description
								+ '</td><td>'
								+ data[i].amount
								+ '</td><td class="noprint">'
								+'<button class="btn center" onclick="deleteIncome('
								+data[i].incomeId+')">'
								+'<i class="fa fa-trash expdelete">'
								+'</i></button>'
								+ '</td></tr>';
						totalIncome = totalIncome
								+ data[i].amount;
					}
					trHTML += '<tr  class="noprint" ><td></td><td></td><td></td><td></td><td>'
							+ '<B>Total Expense : </B>'
							+ '</td><td>'
							+ totalIncome
							+ '</td></tr>';
					$('#thIncomeBody').append(trHTML);
					$('#income').text(totalIncome);
				},
				error : function(data) {
				console.log(data);

				}
			});
			
			$('#incomeMonth').text(' ('+getMonths(dateObj) + ' - '+ getYear(dateObj)+ ')');
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
	                    }, 5000);
	       	         },
	       	         error(jqXHR,textStatus, errorThrown){
	       	         console.log(jqXHR);
	       	         console.log(textStatus);
	       	         console.log(errorThrown);
	       	         }
	       	    });
		}
		
		function deleteIncome(id) {
			 $.ajax({
	       	        url: '/api/expenseTracker/incomeDetail/'+id,
	       	        method: 'delete',
	       	        contentType: 'application/json',
	       	         success: function(){
	       	           $('#deleteIncomestatus').text('Income id '+id+' - Deleted successfully ');
	       	           searchIncome() ;
	       	           setTimeout(function() {
	                        $('#deleteIncomestatus').empty();
	                    }, 5000);
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
			var incomeDetails = document.getElementById('incometable').innerHTML;
			var tableContents = document.getElementById('expensetable').innerHTML;
			var expsummary = document.getElementById('expsummary').innerHTML;
			var incomeTitle = document.getElementById('printIncome').innerHTML;
			document.getElementById('savings').innerHTML = (document.getElementById('income').innerHTML)
			-(document.getElementById('overAllExpense').innerHTML);
			var ovearallStatus = document.getElementById('overAllStatus').innerHTML;
			// getting the original dom
			var originalContents = document.body.innerHTML;
			var selectedMonth = document.getElementById('month').value;
			var selectedIncomeMonth = document.getElementById('incomemonth').value;
			var htmlToPrint = '' +
			'<style type="text/css">' +
	        'table th, table td {' +
	        'border:1px solid #ffc107;' +
	        'padding:0.5em;' +
	        '}' +
	        '</style>';
			
			document.body.innerHTML = htmlToPrint+incomeTitle+incomeDetails +printContents + tableContents+ expsummary+
			ovearallStatus;
			// window print
			window.print();
			// again setting the orginal dom
			document.body.innerHTML = originalContents;
			document.getElementById('month').value= selectedMonth;
			document.getElementById('incomemonth').value= selectedIncomeMonth;
		}
		
		function createIncome() {
			var incomeDate = document.getElementById('incomeDate').value;
			var incomeSource = document.getElementById('incomeSource').value;
			var incomeAmount = document.getElementById('incomeAmount').value;
			var expMonth=null;
			var expYear=null;
			var dateObj = null;
			if(incomeDate && incomeDate != null) {
				dateObj = new Date(incomeDate);
				incomeMonth = getMonths(dateObj);
				incomeYear = getYear(dateObj);
			}
			if(incomeDate && incomeSource && incomeAmount) {
				 var reqObj = {
	        		        "year": incomeYear,
	        		        "month": incomeMonth,
	        		        "incomeDate": incomeDate,
	        		        "amount": incomeAmount,
	        		        "description": incomeSource
	        	   };
	        	   $.ajax({
	          	        url: '/api/expenseTracker/saveIncomeDetails',
	          	        method: 'POST',
	          	        data: JSON.stringify(reqObj),
	          	        dataType: 'json',
	          	        contentType: 'application/json',
	          	         success: function(result){
	          	        	resetIncomeCreateValues();
	          	           $('#createIncomestatus').text("Income entry created successfully. Income id : "+result.incomeId);
	          	           searchIncome() ;
	          	           setTimeout(function() {
	                           $('#createIncomestatus').empty();
	                       }, 5000);
	          	         },
	          	         error(jqXHR,textStatus, errorThrown){
	          	         console.log(jqXHR);
	          	         console.log(textStatus);
	          	         console.log(errorThrown);
	          	         }
	          	    }); 
			} else {
				validatCreateField(document.getElementById('incomeDate'),incomeDate);
	        	validatCreateField(document.getElementById('incomeSource'), incomeSource);
	        	validatCreateField(document.getElementById('incomeAmount'), incomeAmount);
			}
			
		}
		
		function resetIncomeCreateValues() {
			document.getElementById('incomeDate').value= '';
			document.getElementById('incomeSource').value= '';
			document.getElementById('incomeAmount').value= '';
		}
		
		function addPersonalInfo() {
			var docName = document.getElementById('docName').value;
			var docNumber = document.getElementById('docNumber').value;
			var docValidity = document.getElementById('docValidity').value;
			var remarks = document.getElementById('personalRemarks').value;
			if(docName && docNumber&&remarks) {
			 var reqObj = {
     		        "recordName": docName,
     		        "recordNumber": docNumber,
     		        "recordValidity": docValidity,
     		        "remarks": remarks
     	   };
			  $.ajax({
        	        url: '/api/personalDetail/savePersonalDetail',
        	        method: 'POST',
        	        data: JSON.stringify(reqObj),
        	        dataType: 'json',
        	        contentType: 'application/json',
        	         success: function(result){
        	        	resetPersonalDetailValues();
        	           $('#createPersonalDetailId').text("Personal Detail id : "+result.detailId);
        	           displayPersonalData();
        	           setTimeout(function() {
                         $('#createPersonalDetailId').empty();
                     }, 5000);
        	         },
        	         error(jqXHR,textStatus, errorThrown){
        	         console.log(jqXHR);
        	         console.log(textStatus);
        	         console.log(errorThrown);
        	         }
        	    }); 
			} else {
				validatCreateField(document.getElementById('docName'),docName);
				validatCreateField(document.getElementById('docNumber'),docNumber);
				validatCreateField(document.getElementById('personalRemarks'),remarks);
			}
		}
		
		function resetPersonalDetailValues() {
			document.getElementById('docName').value= '';
			document.getElementById('docNumber').value= '';
			document.getElementById('docValidity').value= '';
			document.getElementById('personalRemarks').value= '';
		}
		
		function addAssetInfo() {
			var assetName = document.getElementById('assetName').value;
			var assetDocNum = document.getElementById('assetNumber').value;
			var assetBoughtOn = document.getElementById('boughtOn').value;
			var assetType = document.getElementById('assetType').value;
			var assetQty = document.getElementById('assetQuantity').value;
			var assetVal = document.getElementById('currentValue').value;
			var assetRemarks = document.getElementById('assetRemarks').value;
			
			if(assetName && assetDocNum && assetBoughtOn && assetType && assetQty && assetVal) {
				var reqObj = {
	     		        "assetName": assetName,
	     		        "assetDocumentNumber": assetDocNum,
	     		        "assetBoughtOn": assetBoughtOn,
	     		       "assetType": assetType,
	     		        "assetQty": assetQty,
	     		        "assetCurrentValue": assetVal,
	     		        "remarks": assetRemarks
	     	   };
				$.ajax({
        	        url: '/api/assetDetail/saveAssetDetail',
        	        method: 'POST',
        	        data: JSON.stringify(reqObj),
        	        dataType: 'json',
        	        contentType: 'application/json',
        	         success: function(result){
        	        	 resetAssetCreateValues();
        	           $('#createdAssetId').text("Asset Detail id : "+result.assetId);
        	           displayAssetData() ;
        	           setTimeout(function() {
                         $('#createdAssetId').empty();
                     }, 5000);
        	         },
        	         error(jqXHR,textStatus, errorThrown){
        	         console.log(jqXHR);
        	         console.log(textStatus);
        	         console.log(errorThrown);
        	         }
        	    }); 
			} else {
				validatCreateField(document.getElementById('assetName'),assetName);
				validatCreateField(document.getElementById('assetNumber'),assetDocNum);
				validatCreateField(document.getElementById('boughtOn'),assetBoughtOn);
				validatCreateField(document.getElementById('assetType'),assetType);
				validatCreateField(document.getElementById('assetQuantity'),assetQty);
				validatCreateField(document.getElementById('currentValue'),assetVal);
				validatCreateField(document.getElementById('assetRemarks'),assetRemarks);
			}
		}
		
		function resetAssetCreateValues() {
			document.getElementById('assetName').value= '';
			document.getElementById('assetNumber').value= '';
			document.getElementById('boughtOn').value= '';
			document.getElementById('assetType').value= '';
			document.getElementById('assetQuantity').value= '';
			document.getElementById('currentValue').value= '';
			document.getElementById('assetRemarks').value= '';
		}
		
		function displayPersonalData() {
			$.ajax({
				dataType : "json",
				cache : false,
				headers : {
					Accept : "application/json",
					"Access-Control-Allow-Origin" : "*"
				},
				type : "GET",
				url : "/api/personalDetail/personalDetails",
				success : function(data) {
					 $('#thPersonalDetailBody').empty();
					var trHTML = '';
					for (i = 0; i < data.length; i++) {
						trHTML += '<tr><td>'
								+ data[i].detailId
								+ '</td><td>'
								+ data[i].recordName
								+ '</td><td>'
								+ data[i].recordNumber
								+ '</td><td>'
								+ data[i].recordValidity
								+ '</td><td>'
								+ data[i].remarks
								+ '</td><td class="noprint">'
								+'<button class="btn center noprint" onclick="deletePersonalDetails('
								+data[i].detailId+')">'
								+'<i class="fa fa-trash expdelete">'
								+'</i></button>'
								+ '</td></tr>';
					}
					$('#thPersonalDetailBody').append(trHTML);
				},
				error : function(data) {
				console.log(data);
				}
			});
		}
		
		function deletePersonalDetails(id) {
			 $.ajax({
	       	        url: '/api/personalDetail/'+id,
	       	        method: 'delete',
	       	        contentType: 'application/json',
	       	         success: function(){
	       	           $('#deletePersonalDetailstatus').text('detail id '+id+' - Deleted successfully ');
	       	           setTimeout(function() {
	                        $('#deletePersonalDetailstatus').empty();
	                    }, 5000);
	       	        displayPersonalData();
	       	         },
	       	         error(jqXHR,textStatus, errorThrown){
	       	         console.log(jqXHR);
	       	         console.log(textStatus);
	       	         console.log(errorThrown);
	       	         }
	       	    });
		}
		
		function printPersonalDetail() {
			var printContents = document.getElementById('personalInfo').innerHTML;
			var htmlToPrint = '' +
			'<style type="text/css">' +
	        'table th, table td {' +
	        'border:1px solid #ffc107;' +
	        'padding:0.5em;' +
	        '}' +
	        '</style>';
			// getting the original dom
			var originalContents = document.body.innerHTML;
			document.body.innerHTML = htmlToPrint+printContents;
			// window print
			window.print();
			// again setting the orginal dom
			document.body.innerHTML = originalContents;
		}
		
		function displayAssetData() {
			$.ajax({
				dataType : "json",
				cache : false,
				headers : {
					Accept : "application/json",
					"Access-Control-Allow-Origin" : "*"
				},
				type : "GET",
				url : "/api/assetDetail/assetDetails",
				success : function(data) {
					 $('#thAssetDeatilBody').empty();
					var trHTML = '';
					for (i = 0; i < data.length; i++) {
						trHTML += '<tr><td>'
								+ data[i].assetId
								+ '</td><td>'
								+ data[i].assetName
								+ '</td><td>'
								+ data[i].assetDocumentNumber
								+ '</td><td>'
								+ data[i].assetBoughtOn
								+ '</td><td>'
								+ data[i].assetType
								+ '</td><td>'
								+ data[i].assetQty
								+ '</td><td>'
								+ data[i].assetCurrentValue
								+ '</td><td>'
								+ data[i].remarks
								+ '</td><td class="noprint">'
								+'<button class="btn center noprint" onclick="deleteAssetDetails('
								+data[i].assetId+')">'
								+'<i class="fa fa-trash expdelete">'
								+'</i></button>'
								+ '</td></tr>';
					}
					$('#thAssetDeatilBody').append(trHTML);
				},
				error : function(data) {
				console.log(data);
				}
			});
		}
		
		function deleteAssetDetails(id) {
			 $.ajax({
	       	        url: '/api/assetDetail/'+id,
	       	        method: 'delete',
	       	        contentType: 'application/json',
	       	         success: function(){
	       	           $('#deleteAssetDetailstatus').text('Asset id '+id+' - Deleted successfully ');
	       	           setTimeout(function() {
	                        $('#deleteAssetDetailstatus').empty();
	                    }, 5000);
	       	        displayAssetData();
	       	         },
	       	         error(jqXHR,textStatus, errorThrown){
	       	         console.log(jqXHR);
	       	         console.log(textStatus);
	       	         console.log(errorThrown);
	       	         }
	       	    });
		}
		
		function printAssetDetail() {
			var printContents = document.getElementById('assetInfo').innerHTML;
			var htmlToPrint = '' +
			'<style type="text/css">' +
	        'table th, table td {' +
	        'border:1px solid #ffc107;' +
	        'padding:0.5em;' +
	        '}' +
	        '</style>';
			// getting the original dom
			var originalContents = document.body.innerHTML;
			document.body.innerHTML = htmlToPrint+printContents;
			// window print
			window.print();
			// again setting the orginal dom
			document.body.innerHTML = originalContents;
		}