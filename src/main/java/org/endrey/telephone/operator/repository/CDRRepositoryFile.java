package org.endrey.telephone.operator.repository;

import java.io.IOException;
import java.util.List;

import org.endrey.telephone.operator.entity.CDR;

public interface CDRRepositoryFile {
    void saveToFile(List<CDR> cdr) throws IOException;
    List<CDR> findAll() throws IOException;
    List<CDR> findAllByPhoneNumber(String phoneNumber) throws IOException;
    List<CDR> findAllByPhoneNumberAndMonth(String phoneNumber, int month) throws IOException;
}
