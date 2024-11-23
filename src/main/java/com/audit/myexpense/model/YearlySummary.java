package com.audit.myexpense.model;

import org.springframework.data.annotation.Id;

import java.util.List;

public class YearlySummary {

    @Id
    public Integer year;
    public double expense;
    public double income;
    public double savings;
    public double estimated;
    public List<Category> category;
//    public double planned;
//    public double unPlanned;
//    public double investment;
}
