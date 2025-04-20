package com.audit.myexpense.model;

import org.springframework.data.annotation.Id;

public class MonthlyExpByCatagory {

    @Id
    public String category;
    public double January;
    public double February;
    public double March;
    public double April;
    public double May;
    public double June;
    public double July;
    public double August;
    public double September;
    public double  October;
    public double  November;
    public double  December;
}
