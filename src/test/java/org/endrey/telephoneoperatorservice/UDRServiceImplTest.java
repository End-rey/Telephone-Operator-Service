package org.endrey.telephoneoperatorservice;

import org.endrey.telephone.operator.entity.Abonent;
import org.endrey.telephone.operator.entity.CDR;
import org.endrey.telephone.operator.entity.UDR;
import org.endrey.telephone.operator.enums.CallType;
import org.endrey.telephone.operator.repository.CDRRepositoryFile;
import org.endrey.telephone.operator.repository.UDRRepositoryFile;
import org.endrey.telephone.operator.service.serviceImpl.UDRServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class UDRServiceImplTest {

    private final LocalDateTime startTime = LocalDateTime.ofEpochSecond(1709798657, 0, ZoneOffset.UTC);
    private final LocalDateTime endTime = LocalDateTime.ofEpochSecond(1709799601, 0, ZoneOffset.UTC);
    private final String duration = formatDuration(Duration.between(startTime, endTime));
    private final int month = startTime.getMonthValue();

    @Mock
    private CDRRepositoryFile cdrRepositoryFile;

    @Mock
    private UDRRepositoryFile udrRepositoryFile;

    @InjectMocks
    private UDRServiceImpl udrService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateReport_EmptyCDRList() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(Collections.emptyList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        List<UDR> udrList = udrService.generateReport();

        assertEquals(0, udrList.size());
    }

    @Test
    public void testGenerateReport() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(createTestCDRList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        List<UDR> udrList = udrService.generateReport();

        List<Abonent> abonentList = createTestAbonentList();

        assertEquals(abonentList.size(), udrList.size());
        assertEquals(abonentList.get(0).getPhoneNumber(), udrList.get(0).getMsisdn());
        assertEquals(abonentList.get(1).getPhoneNumber(), udrList.get(1).getMsisdn());
        assertEquals(duration, udrList.get(0).getOutcomingCall().getTotalTime());
        assertEquals(duration, udrList.get(0).getIncomingCall().getTotalTime());
        assertEquals(duration, udrList.get(1).getOutcomingCall().getTotalTime());
        assertEquals(duration, udrList.get(1).getIncomingCall().getTotalTime());
    }

    @Test
    public void testGenerateReportByPhoneNumber() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(createTestCDRList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        List<Abonent> abonentList = createTestAbonentList();
        Map<Integer, UDR> udrMap = udrService.generateReportByPhoneNumber(abonentList.get(0).getPhoneNumber());

        assertEquals(12, udrMap.size());
        assertEquals(abonentList.get(0).getPhoneNumber(), udrMap.get(month).getMsisdn());
        assertEquals("00:00:00", udrMap.get(1).getOutcomingCall().getTotalTime());
        assertEquals("00:00:00", udrMap.get(1).getIncomingCall().getTotalTime());
        assertEquals(duration, udrMap.get(month).getOutcomingCall().getTotalTime());
        assertEquals(duration, udrMap.get(month).getIncomingCall().getTotalTime());
    }

    @Test
    public void testGenerateReportByPhoneNumber_EmptyCDRList() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(Collections.emptyList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        List<Abonent> abonentList = createTestAbonentList();
        Map<Integer, UDR> udrMap = udrService.generateReportByPhoneNumber(abonentList.get(0).getPhoneNumber());

        assertEquals(0, udrMap.size());
    }

    @Test
    public void testGenerateReportByPhoneNumber_InavalidPhoneNumber() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(createTestCDRList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
        
        Map<Integer, UDR> udrMap = udrService.generateReportByPhoneNumber("1");

        assertEquals(0, udrMap.size());
    }

    @Test
    public void testGenerateReportByPhoneNumberAndMonth() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(createTestCDRList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        List<Abonent> abonentList = createTestAbonentList();

        List<UDR> udrList = udrService.generateReportByPhoneNumberAndMonth(abonentList.get(0).getPhoneNumber(), month);

        assertEquals(1, udrList.size());
        assertEquals(abonentList.get(0).getPhoneNumber(), udrList.get(0).getMsisdn());
        assertEquals(duration, udrList.get(0).getOutcomingCall().getTotalTime());
        assertEquals(duration, udrList.get(0).getIncomingCall().getTotalTime());
    }

    @Test
    public void testGenerateReportByPhoneNumberAndMonth_EmptyCDRList() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(Collections.emptyList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        List<Abonent> abonentList = createTestAbonentList();

        List<UDR> udrList = udrService.generateReportByPhoneNumberAndMonth(abonentList.get(0).getPhoneNumber(), month);

        assertEquals(0, udrList.size());
    }

    @Test
    public void testGenerateReportByPhoneNumberAndMonth_InvalidPhoneNumber() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(createTestCDRList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        List<UDR> udrList = udrService.generateReportByPhoneNumberAndMonth("1", month);

        assertEquals(0, udrList.size());
    }

    @Test
    public void testGenerateReportByPhoneNumberAndMonth_MonthWithoutCalls() {
        try {
            when(cdrRepositoryFile.findAll()).thenReturn(createTestCDRList());
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }

        List<Abonent> abonentList = createTestAbonentList();

        List<UDR> udrList = udrService.generateReportByPhoneNumberAndMonth(abonentList.get(0).getPhoneNumber(), 1);

        assertEquals(1, udrList.size());
        assertEquals(abonentList.get(0).getPhoneNumber(), udrList.get(0).getMsisdn());
        assertEquals("00:00:00", udrList.get(0).getOutcomingCall().getTotalTime());
        assertEquals("00:00:00", udrList.get(0).getIncomingCall().getTotalTime());
    }

    private List<CDR> createTestCDRList() {
        List<Abonent> abonentList = createTestAbonentList();

        List<CDR> cdrList = new ArrayList<>(List.of(
            CDR.builder()
                .phoneNumber(abonentList.get(0))
                .callType(CallType.INCOMING_CALL)
                .startTime(startTime)
                .endTime(endTime)
                .build(),
            CDR.builder()
                .phoneNumber(abonentList.get(1))
                .callType(CallType.OUTCOMING_CALL)
                .startTime(startTime)
                .endTime(endTime)
                .build(),
            CDR.builder()
                .phoneNumber(abonentList.get(0))
                .callType(CallType.OUTCOMING_CALL)
                .startTime(startTime)
                .endTime(endTime)
                .build(),
            CDR.builder()
                .phoneNumber(abonentList.get(1))
                .callType(CallType.INCOMING_CALL)
                .startTime(startTime)
                .endTime(endTime)
                .build()
        ));
        return cdrList;
    }

    private List<Abonent> createTestAbonentList() {
        List<Abonent> abonentList = new ArrayList<>(List.of(
            new Abonent("123456789"),
            new Abonent("987654321")
        ));
        return abonentList;
    }

    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.minusHours(hours).toMinutes();
        long seconds = duration.minusHours(hours).minusMinutes(minutes).getSeconds();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
