package rtp.demo.workflow.transformer;

import com.google.gson.Gson;
import fraud.analysis.demo.transaction.Transaction;
import iso.std.iso._20022.tech.xsd.pacs_008_001.CreditTransferTransaction25;
import iso.std.iso._20022.tech.xsd.pacs_008_001.FIToFICustomerCreditTransferV06;
import iso.std.iso._20022.tech.xsd.pacs_008_001.GroupHeader70;
import org.apache.camel.Body;
import rtp.demo.creditor.domain.payments.Payment;

import java.io.InputStream;

public class ParseCaseData {
    public String process(@Body String body) {

        System.out.println("From the parser"+ "{\"case-data\":"+body+"}");

        return "{\"case-data\":"+body+"}";
    }





}
