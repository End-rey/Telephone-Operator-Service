package org.endrey.telephone.operator.service.serviceImpl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.endrey.telephone.operator.entity.Abonent;
import org.endrey.telephone.operator.entity.CDR;
import org.endrey.telephone.operator.enums.CallType;
import org.endrey.telephone.operator.repository.AbonentRepositoryH2;
import org.endrey.telephone.operator.repository.CDRRepositoryFile;
import org.endrey.telephone.operator.repository.CDRRepositoryH2;
import org.endrey.telephone.operator.service.CDRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for managing CDR (Call Detail Records) operations.
 * It contains information about a phone call like a
 * phone number, start and end time of the call, type of call (incoming or
 * outcoming)
 * 
 */
@Service
public class CDRServiceImpl implements CDRService {

    @Autowired
    private CDRRepositoryH2 cdrRepositoryH2;

    @Autowired
    private CDRRepositoryFile cdrRepositoryFile;

    @Autowired
    private AbonentRepositoryH2 abonetRepository;

    /**
     * Generate CDR (Call Detail Records) for the period of one year (12 months).
     * It generates CDR records for each phone number of the abonents in the system
     * and for each month of the year.
     */
    @Override
    public void generateCDRForPeriod() {
        List<Abonent> abonentsPhoneNumbers = abonetRepository.findAll();

        try {
            for (int month = 1; month <= 12; month++) {
                generateCDRByPhoneNumbersAndMonth(month, abonentsPhoneNumbers);
            }
            System.out.println("All CDR generated successfully");
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Generate CDR (Call Detail Records) for the specified month of the year.
     * It generates CDR records for each phone number of the abonents in the system
     * and for the specified month of the year.
     * 
     * @param month - month of the year (from 1 to 12)
     */
    @Override
    public void generateCDRByMonth(int month) {
        List<Abonent> abonentsPhoneNumbers = abonetRepository.findAll();

        try {
            generateCDRByPhoneNumbersAndMonth(month, abonentsPhoneNumbers);
            System.out.println("CDR generated successfully");
        } catch (IllegalArgumentException e) {
            System.err.println("ERROR: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Generate CDR (Call Detail Records) for the specified month of the year and
     * for each phone number of the abonents in the system.
     * 
     * @param month    - month of the year (from 1 to 12)
     * @param abonents - list of phone numbers of the abonents
     * @throws IOException - if there is an error while writing to the file
     */
    private void generateCDRByPhoneNumbersAndMonth(int month, List<Abonent> abonents)
            throws IOException, IllegalArgumentException {
        int year = LocalDateTime.now().getYear();
        int numAbonents = abonents.size();
        if (numAbonents == 0) {
            throw new IllegalArgumentException("There are no abonents in the system");
        }

        Random random = new Random();

        int numRecords = random.nextInt(100) + 100;

        List<CDR> cdrList = new ArrayList<>();

        for (int i = 0; i < numRecords; i++) {
            String callType = (random.nextBoolean() ? "01" : "02");

            Abonent phoneNumber = abonents.get(random.nextInt(numAbonents));

            int day = random.nextInt(Month.of(month).maxLength()) + 1;

            LocalDateTime startTime = LocalDateTime.of(year, month, day, random.nextInt(24), random.nextInt(60));
            LocalDateTime endTime = startTime.plusMinutes(random.nextInt(60));

            CDR cdr = CDR.builder()
                    .callType(CallType.getByCode(callType))
                    .phoneNumber(phoneNumber)
                    .startTime(startTime)
                    .endTime(endTime)
                    .build();

            cdrList.add(cdr);

        }

        cdrList.sort((cdr1, cdr2) -> {
            if (cdr1.getStartTime().isBefore(cdr2.getStartTime())) {
                return -1;
            } else if (cdr1.getStartTime().isAfter(cdr2.getStartTime())) {
                return 1;
            } else {
                return 0;
            }
        });

        cdrRepositoryFile.saveToFile(cdrList);
        cdrRepositoryH2.saveAll(cdrList);
    }
}
