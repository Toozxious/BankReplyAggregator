/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package bankreplyaggrigator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import utilities.xml.xmlMapper;

/**
 *
 * @author Kaboka
 */
public class LoanAggregate implements Aggregate {

    private BankLoan bankLoan;
    
    public LoanAggregate(BankLoan bankLoan){
        this.bankLoan = new BankLoan();
    }
    
    @Override
    public void addMessage(String message) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();
            Document doc = xmlMapper.getXMLDocument(message);
            Loan loan = new Loan();
            loan.setSsn(xpath.compile("/LoanResponse/ssn").evaluate(doc));
            loan.setBankName(xpath.compile("/LoanResponse/bankName").evaluate(doc));
            System.out.println(xpath.compile("/LoanResponse/bankName").evaluate(doc));
            loan.setInterestRate(Double.parseDouble(xpath.compile("/LoanResponse/intrestRate").evaluate(doc)));
            bankLoan.addLoan(loan);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(LoanAggregate.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public boolean isComplete() {
        return bankLoan.isComplete();
    }

    @Override
    public String getResultMessage() {
        String body = "";
        try {
            
            Loan loan = bankLoan.getBestLoan();
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            
            Element loanResponse = doc.createElement("LoanResponse");
            doc.appendChild(loanResponse);
            Element ssn = doc.createElement("ssn");
            ssn.appendChild(doc.createTextNode(loan.getSsn()));
            Element bankName = doc.createElement("bankName");
            bankName.appendChild(doc.createTextNode(loan.getBankName()));
            Element interestRate = doc.createElement("intrestRate");
            interestRate.appendChild(doc.createTextNode(Double.toString(loan.getInterestRate())));
            loanResponse.appendChild(ssn);
            loanResponse.appendChild(bankName);
            loanResponse.appendChild(interestRate);
            
            body = xmlMapper.getStringFromDoc(doc);
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LoanAggregate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return body;
    }
    
}
