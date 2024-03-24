package org.endrey.telephone.operator.repository;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import org.endrey.telephone.operator.entity.CDR;

public interface CDRRepositoryFile {
    void saveToFile(List<CDR> cdr) throws IOException;
    List<CDR> findAll() throws NumberFormatException, FileNotFoundException, IOException;
    List<CDR> findAllByPhoneNumber(String phoneNumber) throws NumberFormatException, FileNotFoundException, IOException;
    List<CDR> findAllByPhoneNumberAndMonth(String phoneNumber, int month) throws NumberFormatException, FileNotFoundException, IOException;
}
