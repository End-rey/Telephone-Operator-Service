package org.endrey.telephone.operator.service.serviceImpl;

import java.io.FileNotFoundException;
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

/**
 * Service that generates the Usage Data Report (UDR) 
 * based on the CDR data stored in the folder CDR.
 * It contains information about phone number and duration of the call.
 */
@Service
public class UDRServiceImpl implements UDRService {

    @Autowired
    private CDRRepositoryFile cdrRepositoryFile;

    @Autowired
    private UDRRepositoryFile udrRepositoryFile;

    /**
	 * Save all UDRs in folder /reports in JSON format.
     * Then make a list of UDRs with all abonents and 
     * there total call times for the entire tariffed period.
     * 
	 * @return the list of UDRs
	 */
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
        } catch (FileNotFoundException e) {
            System.out.println("There is no files CDR");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return udrList;
    }

    /**
	 * Save all UDRs in folder /reports in JSON format.
     * Generate UDR for one abonent and 
     * his call times for each month.
     * 
     * @param msisdn - phone number of abonent
     * @return report for one abonent in map with key - month and value - UDR
	 */
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
        } catch (FileNotFoundException e) {
            System.out.println("There is no files CDR");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return udrMap;
    }

    /**
     * Save all UDRs in folder /reports in JSON format.
     * Generate UDR report for one abonent for one month.
     *
     * @param msisdn - phone number of abonent
     * @param month - month of report
     * @return list of UDRs for one abonent for one month
     */
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
        } catch (FileNotFoundException e) {
            System.out.println("There is no files CDR");
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
        }
        return udrList;
    }
    
    /**
     * Format Duration object to string in format "HH:mm:ss"
     * @param duration - duration to format
     * @return formatted duration
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Parse all CDR files and group them by abonent phone number and month
     * @param cdrList - list of CDRs to parse
     * @return map with abonent phone number as key and 
     * map with month as key and array of incoming and outcoming call duration as value
     */
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

    /**
     * Save UDR to file
     * @param phoneNumber - phone number of abonent
     * @param monthDuration - pair of incoming and outcoming call duration for one month
     * @throws IOException
     */
    private void saveToFile(String phoneNumber, Map.Entry<Integer, Duration[]> monthDuration) throws IOException {

        UDR udr = UDR.builder()
                    .msisdn(phoneNumber)
                    .incomingCall(new TotalTime(formatDuration(monthDuration.getValue()[0])))
                    .outcomingCall(new TotalTime(formatDuration(monthDuration.getValue()[1])))
                    .build();
        udrRepositoryFile.saveToFile(udr, monthDuration.getKey());
    }
}
