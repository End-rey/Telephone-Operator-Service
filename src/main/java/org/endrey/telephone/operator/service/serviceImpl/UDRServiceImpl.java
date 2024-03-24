package org.endrey.telephone.operator.service.serviceImpl;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.endrey.telephone.operator.entity.CDR;
import org.endrey.telephone.operator.entity.TotalTime;
import org.endrey.telephone.operator.entity.UDR;
import org.endrey.telephone.operator.enums.CallType;
import org.endrey.telephone.operator.repository.CDRRepositoryFile;
import org.endrey.telephone.operator.repository.UDRRepositoryFile;
import org.endrey.telephone.operator.service.UDRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UDRServiceImpl implements UDRService {

    @Autowired
    private CDRRepositoryFile cdrRepositoryFile;

    @Autowired
    private UDRRepositoryFile udrRepositoryFile;

    @Override
    public List<UDR> generateReport() {
        List<UDR> udrList = new ArrayList<>();
        try {
            List<CDR> cdrList = cdrRepositoryFile.findAll();

            Map<String, Map<Integer, Duration[]>> abonentSet = parseCdrFiles(cdrList);

            for (Map.Entry<String, Map<Integer, Duration[]>> abonentEntry : abonentSet.entrySet()) {
                String phoneNumber = abonentEntry.getKey();
                Duration incomingCall = Duration.ZERO;
                Duration outcomingCall = Duration.ZERO;
                for (Map.Entry<Integer, Duration[]> monthDuration : abonentEntry.getValue().entrySet()) {
                    saveToFile(phoneNumber, monthDuration);
                    incomingCall = incomingCall.plus(monthDuration.getValue()[0]);
                    outcomingCall = outcomingCall.plus(monthDuration.getValue()[1]);
                }
                UDR udr = UDR.builder()
                        .msisdn(phoneNumber)
                        .incomingCall(new TotalTime(formatDuration(incomingCall)))
                        .outcomingCall(new TotalTime(formatDuration(outcomingCall)))
                        .build();
                udrList.add(udr);
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return udrList;
    }

    @Override
    public Map<Integer, UDR> generateReportByPhoneNumber(String msisdn) {
        Map<Integer, UDR> udrMap = new HashMap<>();

        try {
            List<CDR> cdrList = cdrRepositoryFile.findAll();

            Map<String, Map<Integer, Duration[]>> abonentSet = parseCdrFiles(cdrList);

            for (Map.Entry<String, Map<Integer, Duration[]>> abonentEntry : abonentSet.entrySet()) {
                String phoneNumber = abonentEntry.getKey();
                for (Map.Entry<Integer, Duration[]> monthDuration : abonentEntry.getValue().entrySet()) {
                    saveToFile(phoneNumber, monthDuration);
                    if (phoneNumber.equals(msisdn)){
                        UDR udr = UDR.builder()
                                .msisdn(phoneNumber)
                                .incomingCall(new TotalTime(formatDuration(monthDuration.getValue()[0])))
                                .outcomingCall(new TotalTime(formatDuration(monthDuration.getValue()[1])))
                                .build();
                        udrMap.put(monthDuration.getKey(), udr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return udrMap;
    }

    @Override
    public List<UDR> generateReportByPhoneNumberAndMonth(String msisdn, int month) {
        List<UDR> udrList = new ArrayList<>();
        try {
            List<CDR> cdrList = cdrRepositoryFile.findAll();

            Map<String, Map<Integer, Duration[]>> abonentSet = parseCdrFiles(cdrList);

            for (Map.Entry<String, Map<Integer, Duration[]>> abonentEntry : abonentSet.entrySet()) {
                String phoneNumber = abonentEntry.getKey();
                Duration incomingCall = Duration.ZERO;
                Duration outcomingCall = Duration.ZERO;
                for (Map.Entry<Integer, Duration[]> monthDuration : abonentEntry.getValue().entrySet()) {
                    saveToFile(phoneNumber, monthDuration);
                    if (monthDuration.getKey() != month){
                        continue;
                    }
                    incomingCall = incomingCall.plus(monthDuration.getValue()[0]);
                    outcomingCall = outcomingCall.plus(monthDuration.getValue()[1]);
                }
                if (!phoneNumber.equals(msisdn)){
                    continue;
                }
                UDR udr = UDR.builder()
                        .msisdn(phoneNumber)
                        .incomingCall(new TotalTime(formatDuration(incomingCall)))
                        .outcomingCall(new TotalTime(formatDuration(outcomingCall)))
                        .build();
                udrList.add(udr);
            }
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return udrList;
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private Map<String, Map<Integer, Duration[]>> parseCdrFiles(List<CDR> cdrList) {
        Map<String, Map<Integer, Duration[]>> abonentSet = new HashMap<>();
        for (CDR cdr : cdrList) {
            String phoneNumber = cdr.getPhoneNumber().getPhoneNumber();
            int index;
            if (cdr.getCallType().equals(CallType.INCOMING_CALL)) {
                index = 0;
            } else {
                index = 1;
            }

            int month = cdr.getStartTime().getMonthValue();
            Duration seconds = Duration.between(cdr.getStartTime(), cdr.getEndTime());
            if (!abonentSet.containsKey(phoneNumber)) {
                abonentSet.put(phoneNumber, new HashMap<Integer, Duration[]>());
                for (int i = 1; i <= 12; i++) {
                    abonentSet.get(phoneNumber).put(i, new Duration[]{Duration.ZERO, Duration.ZERO});
                }
            }
            abonentSet.get(phoneNumber).get(month)[index] = abonentSet.get(phoneNumber).get(month)[index].plus(seconds);
        }
        return abonentSet;
    }

    private void saveToFile(String phoneNumber, Map.Entry<Integer, Duration[]> monthDuration) throws IOException {
        UDR udr = UDR.builder()
                    .msisdn(phoneNumber)
                    .incomingCall(new TotalTime(formatDuration(monthDuration.getValue()[0])))
                    .outcomingCall(new TotalTime(formatDuration(monthDuration.getValue()[1])))
                    .build();
        udrRepositoryFile.saveToFile(udr, monthDuration.getKey());
    }
}
