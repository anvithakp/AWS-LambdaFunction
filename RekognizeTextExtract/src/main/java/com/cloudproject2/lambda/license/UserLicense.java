package com.cloudproject2.lambda.license;

import java.util.Date;

public class UserLicense {

    private String license;
    private String firstname;
    private String lastname;
    private Date expiryDate;

    public String getLicense() {
        return license;
    }

    public UserLicense setLicense(String license) {
        this.license = license;
        return this;
    }

    public String getFirstname() {
        return firstname;
    }

    public UserLicense setFirstname(String firstname) {
        this.firstname = firstname;
        return this;
    }

    public String getLastname() {
        return lastname;
    }

    public UserLicense setLastname(String lastname) {
        this.lastname = lastname;
        return this;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public UserLicense setExpiryDate(Date expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public boolean isComplete() {
        return lastname != null && firstname != null && expiryDate != null && license != null;
    }
}
