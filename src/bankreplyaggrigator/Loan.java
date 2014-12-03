/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bankreplyaggrigator;

/**
 *
 * @author Kaboka
 */
public class Loan {
    double interestRate;
    String bankName;
    String ssn;
    
    public void setInterestRate(double interestRate){
        this.interestRate = interestRate;
    }

    public double getInterestRate() {
        return interestRate;
    }

    public String getBankName() {
        return bankName;
    }

    public void setBankName(String bankName) {
        this.bankName = bankName;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
    
    
    
}
