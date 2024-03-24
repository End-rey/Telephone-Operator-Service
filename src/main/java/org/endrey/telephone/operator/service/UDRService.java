package org.endrey.telephone.operator.service;

import java.util.List;
import java.util.Map;

import org.endrey.telephone.operator.entity.UDR;

public interface UDRService {
    List<UDR> generateReport();
    Map<Integer, UDR> generateReportByPhoneNumber(String msisdn);
    List<UDR> generateReportByPhoneNumberAndMonth(String msisdn, int month);
}
