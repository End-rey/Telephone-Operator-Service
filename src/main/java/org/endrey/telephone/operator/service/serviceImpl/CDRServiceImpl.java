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

@Service
public class CDRServiceImpl implements CDRService {

    @Autowired
    private CDRRepositoryH2 cdrRepositoryH2;

    @Autowired
    private CDRRepositoryFile cdrRepositoryFile;

    @Autowired
    private AbonentRepositoryH2 abonetRepository;

    @Override
    public void generateCDRForPeriod() {
        List<Abonent> abonentsPhoneNumbers = abonetRepository.findAll().stream().toList();

        try {
        for (int month = 1; month <= 12; month++) {
                generateCDRByPhoneNumbersAndMonth(month, abonentsPhoneNumbers);
        }
        System.out.println("All CDR generated successfully");
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    @Override
    public void generateCDRByMonth(int month) {
        List<Abonent> abonentsPhoneNumbers = abonetRepository.findAll().stream().toList();

        try {
            generateCDRByPhoneNumbersAndMonth(month, abonentsPhoneNumbers);
            System.out.println("CDR generated successfully");
        } catch (IOException e) {
            System.err.println("ERROR: " + e.getMessage());
        }
    }

    

    private void generateCDRByPhoneNumbersAndMonth(int month, List<Abonent> abonents) throws IOException {
        int year = LocalDateTime.now().getYear();
        int numAbonents = abonents.size();

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
