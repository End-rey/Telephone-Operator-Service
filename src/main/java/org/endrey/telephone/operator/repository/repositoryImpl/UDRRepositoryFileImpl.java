package org.endrey.telephone.operator.repository.repositoryImpl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.endrey.telephone.operator.entity.UDR;
import org.endrey.telephone.operator.repository.UDRRepositoryFile;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;;

/**
 * This class is used to write UDR data to files in JSON format.
 *
 */
@Repository
public class UDRRepositoryFileImpl implements UDRRepositoryFile {

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Saves the UDR data to a file.
     * @param udr UDR data
     * @param month month for which the data is stored in the file (1-12)
     * @throws IOException if there is an error while writing to the file
     */
    @Override
    public void saveToFile(UDR udr, int month) throws IOException {
        String fileName = getFileName(udr.getMsisdn(), month);

        objectMapper.writeValue(Paths.get(fileName).toFile(), udr);
    }
    
    /**
     * Returns the file name for the given phone number and month.
     *
     * @param phoneNumber phone number of the user
     * @param month month for which the data is stored in the file (1-12)
     * @return file name
     * @throws IOException if there is an error while creating the directory
     */
    private String getFileName(String phoneNumber, int month) throws IOException {

        String directory = "./reports/";
        Files.createDirectories(Paths.get(directory));

        return String.format(directory + phoneNumber + "_%d.json", month);
    }

}
