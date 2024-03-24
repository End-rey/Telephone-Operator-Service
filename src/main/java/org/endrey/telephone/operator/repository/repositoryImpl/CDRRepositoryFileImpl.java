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
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import java.nio.file.Paths;
import java.nio.file.Files;

@Repository
public class CDRRepositoryFileImpl implements CDRRepositoryFile {

    @Autowired
    private AbonentRepositoryH2 abonentRepository;

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

    private String getFileName(int month) throws IOException {

        String directory = "./CDR/";
        Files.createDirectories(Paths.get(directory));

        return String.format(directory + "/%d.txt", month);
    }

    @Override
    public List<CDR> findAllByPhoneNumberAndMonth(String phoneNumber, int month) throws IOException {
        String fileName = getFileName(month);

        return parseFile(fileName, phoneNumber);
    }

    @Override
    public List<CDR> findAll() throws IOException {
        List<CDR> cdrList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            cdrList.addAll(findAllByPhoneNumberAndMonth("", month));
        }

        return cdrList;
    }

    @Override
    public List<CDR> findAllByPhoneNumber(String phoneNumber) throws IOException {

        List<CDR> cdrList = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            String fileName = getFileName(month);

            cdrList.addAll(parseFile(fileName, phoneNumber));
        }

        return cdrList;
    }

    private List<CDR> parseFile(String fileName, String phoneNumber) throws IOException {
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
