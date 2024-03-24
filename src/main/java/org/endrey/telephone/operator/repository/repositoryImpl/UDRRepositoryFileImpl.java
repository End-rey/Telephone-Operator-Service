package org.endrey.telephone.operator.repository.repositoryImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.endrey.telephone.operator.entity.UDR;
import org.endrey.telephone.operator.repository.UDRRepositoryFile;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;;

@Repository
public class UDRRepositoryFileImpl implements UDRRepositoryFile {

    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void saveToFile(UDR udr, int month) throws IOException {
        String fileName = getFileName(udr.getMsisdn(), month);

        objectMapper.writeValue(Paths.get(fileName).toFile(), udr);
    }
    
    private String getFileName(String phoneNumber, int month) throws IOException {

        String directory = "./reports/";
        Files.createDirectories(Paths.get(directory));

        return String.format(directory + phoneNumber + "_%d.json", month);
    }

}
