package org.endrey.telephone.operator.repository;

import java.io.IOException;

import org.endrey.telephone.operator.entity.UDR;

public interface UDRRepositoryFile {
    void saveToFile(UDR udr, int month) throws IOException;
}
