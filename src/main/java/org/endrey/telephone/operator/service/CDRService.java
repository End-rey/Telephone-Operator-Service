package org.endrey.telephone.operator.service;

public interface CDRService {
    void generateCDRForPeriod();
    void generateCDRByMonth(int month);
}
