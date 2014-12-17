/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bankreplyaggrigator;

import java.util.ArrayList;

/**
 *
 * @author Kaboka
 */
public class BankLoan {

    private ArrayList<Loan> loans = new ArrayList();
    private boolean firstLoan = true;
    private long startTime = 0;

    public void addLoan(Loan loan) {
        if (firstLoan) {
            firstLoan = false;
            startTime = System.currentTimeMillis();
        }
        loans.add(loan);
        System.out.println(loans.size() + " loans recived.");
    }

    public boolean isComplete() {
        long currentTime = System.currentTimeMillis();
        return (loans.size() >= 4) || (startTime + 5000 <= currentTime); // loan strategy
    }

    public Loan getBestLoan() {
        Loan bestLoan = loans.get(0);
        for (int i = 1; i < loans.size(); i++) {
            if (bestLoan.getInterestRate() < loans.get(i).getInterestRate()) {
                bestLoan = loans.get(i);
            }
        }
        return bestLoan;
    }
}
