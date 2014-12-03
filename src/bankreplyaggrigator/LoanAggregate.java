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
        bankLoan = new BankLoan();
    }
    
    @Override
    public void addMessage(String message) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            XPath xpath = XPathFactory.newInstance().newXPath();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(message);
            Loan loan = new Loan();
            loan.setSsn(xpath.compile("/LoanResponse/ssn").evaluate(doc));
            loan.setBankName(xpath.compile("/LoanResponse/bankName").evaluate(doc));
            loan.setInterestRate(Double.parseDouble(xpath.compile("/LoanResponse/interestRate").evaluate(doc)));
            bankLoan.addLoan(loan);
        } catch (SAXException ex) {
            Logger.getLogger(LoanAggregate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LoanAggregate.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LoanAggregate.class.getName()).log(Level.SEVERE, null, ex);
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
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();
            Node loanResponse = doc.createElement("LoanResponse");
            loanResponse.appendChild(doc.createElement("ssn")).appendChild(doc.createTextNode(loan.getSsn()));
            loanResponse.appendChild(doc.createElement("bankName")).appendChild(doc.createTextNode(loan.getBankName()));
            loanResponse.appendChild(doc.createElement("interestRate")).appendChild(doc.createTextNode(String.valueOf(loan.getInterestRate())));
            
            body = xmlMapper.getStringFromDoc(doc);
            
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(LoanAggregate.class.getName()).log(Level.SEVERE, null, ex);
        }
        return body;
    }
    
}
