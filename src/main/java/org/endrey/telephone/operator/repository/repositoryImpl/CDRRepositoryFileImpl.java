package org.endrey.telephone.operator.repository.repositoryImpl;

import org.endrey.telephone.operator.entity.Abonent;
import org.endrey.telephone.operator.entity.CDR;
import org.endrey.telephone.operator.enums.CallType;
import org.endrey.telephone.operator.repository.AbonentRepositoryH2;
import org.endrey.telephone.operator.repository.CDRRepositoryFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Paths;
import java.nio.file.Files;

@Repository
/**
 * This class is used to read and write CDR data from/to files in txt format.
 *
 */
public class CDRRepositoryFileImpl implements CDRRepositoryFile {

    @Autowired
    private AbonentRepositoryH2 abonentRepository;

    /**
     * This method saves the list of CDR entities to file in txt format.
     * It saves one file per month.
     * @param cdrList the list of CDR entities to save
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void saveToFile(List<CDR> cdrList) throws IOException {
        Map<Integer, List<CDR>> cdrMap = cdrList.stream().collect(Collectors.groupingBy(cdr -> cdr.getStartTime().getMonthValue()));

        for (int month : cdrMap.keySet()) {
            String fileName = getFileName(month);
            List<CDR> monthCDR = cdrMap.get(month);

            try (FileWriter writer = new FileWriter(fileName)) {
                for (CDR cdr : monthCDR) {
                    String cdrRecord = cdr.getCallType().getCode() + "," +
                            cdr.getPhoneNumber().getPhoneNumber() + ", " +
                            cdr.getStartTime().toEpochSecond(ZoneOffset.UTC) + ", " +
                            cdr.getEndTime().toEpochSecond(ZoneOffset.UTC) + "\n";
        
                    writer.write(cdrRecord);
                }
            }
        }
        
    }

    /**
     * This method returns the file name for the specified month in the format
     * "./CDR/<month>.txt".
     *
     * @param month the month number
     * @return the file name
     * @throws IOException if an I/O error occurs
     */
    private String getFileName(int month) throws IOException {

        String directory = "./CDR/";
        Files.createDirectories(Paths.get(directory));

        return String.format(directory + "/%d.txt", month);
    }

    /**
     * This method returns a list of all CDR objects stored in the CDR file.
     * 
     * @return a list of all CDR objects
     * @throws NumberFormatException if the unix time is not a valid number
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an I/O error occurs
     */
    @Override
    public List<CDR> findAll() throws NumberFormatException, FileNotFoundException, IOException {

        List<CDR> cdrList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            cdrList.addAll(findAllByPhoneNumberAndMonth("", month));
        }

        return cdrList;
    }

    /**
     * This method returns a list of CDR objects for a specific phone number
     * from all CDR files.
     * 
     * @param phoneNumber the phone number to filter by
     * @return a list of CDR objects
     * @throws NumberFormatException if the unix time is not a valid number
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an I/O error occurs
     */
    @Override
    public List<CDR> findAllByPhoneNumber(String phoneNumber)
            throws NumberFormatException, FileNotFoundException, IOException {

        List<CDR> cdrList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            cdrList.addAll(findAllByPhoneNumberAndMonth(phoneNumber, month));
        }

        return cdrList;
    }
    
    /**
     * This method returns a list of CDR objects for a specific phone number
     * and month from the CDR file.
     * 
     * @param phoneNumber the phone number to filter by
     * @param month the month to filter by
     * @return a list of CDR objects
     * @throws NumberFormatException if the unix time is not a valid number
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an I/O error occurs
     */
    @Override
    public List<CDR> findAllByPhoneNumberAndMonth(String phoneNumber, int month)
            throws NumberFormatException, FileNotFoundException, IOException {
        String fileName = getFileName(month);
        if (!Files.exists(Paths.get(fileName))) {
            return new ArrayList<>();
        }
        return parseFile(fileName, phoneNumber);
    }

    /**
     * This method parses a CDR file and returns a list of CDR objects
     * based on the phone number filter.
     * If the phoneNumber parameter is empty, it will return all CDR objects
     * from the file.
     *
     * @param fileName the name of the CDR file
     * @param phoneNumber the phone number to filter by
     * @return a list of CDR objects
     * @throws NumberFormatException if the unix time is not a valid number
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an I/O error occurs
     */
    private List<CDR> parseFile(String fileName, String phoneNumber)
            throws NumberFormatException, FileNotFoundException, IOException {
        List<CDR> cdrList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");

                if (phoneNumber != "" && !parts[1].equals(phoneNumber)) {
                    continue;
                }

                Abonent abonent = abonentRepository.findByPhoneNumber(parts[1]);
                if (abonent == null) {
                    System.err.println("Abonent not found: " + parts[1]);
                    continue;
                }

                CDR cdr = CDR.builder()
                        .callType(CallType.getByCode(parts[0]))
                        .phoneNumber(abonent)
                        .startTime(LocalDateTime.ofEpochSecond(Long.parseLong(parts[2].strip()), 0, ZoneOffset.UTC))
                        .endTime(LocalDateTime.ofEpochSecond(Long.parseLong(parts[3].strip()), 0, ZoneOffset.UTC))
                        .build();

                cdrList.add(cdr);
            }
        }
        return cdrList;
    }
}
