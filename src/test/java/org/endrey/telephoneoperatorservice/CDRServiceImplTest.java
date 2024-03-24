package org.endrey.telephoneoperatorservice;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.endrey.telephone.operator.entity.Abonent;
import org.endrey.telephone.operator.repository.AbonentRepositoryH2;
import org.endrey.telephone.operator.repository.CDRRepositoryFile;
import org.endrey.telephone.operator.repository.CDRRepositoryH2;
import org.endrey.telephone.operator.service.serviceImpl.CDRServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CDRServiceImplTest {
    @Mock
    private CDRRepositoryH2 cdrRepositoryH2;

    @Mock
    private CDRRepositoryFile cdrRepositoryFile;

    @Mock
    private AbonentRepositoryH2 abonentRepository;

    @InjectMocks
    private CDRServiceImpl cdrService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGenerateCDRForPeriod_AbonentListEmpty() throws IOException {
        List<Abonent> abonents = Collections.emptyList();
        when(abonentRepository.findAll()).thenReturn(abonents);

        cdrService.generateCDRForPeriod();

        verify(cdrRepositoryFile, times(0)).saveToFile(anyList());
        verify(cdrRepositoryH2, times(0)).saveAll(anyList());
    }

    @Test
    public void testGenerateCDRForPeriod() throws IOException {
        List<Abonent> abonents = createTestAbonentList();
        when(abonentRepository.findAll()).thenReturn(abonents);

        cdrService.generateCDRForPeriod();

        verify(cdrRepositoryFile, times(12)).saveToFile(anyList());
        verify(cdrRepositoryH2, times(12)).saveAll(anyList());
    }

    @Test
    public void testGenerateCDRByMonth_AbonentListEmpty() throws IOException {
        List<Abonent> abonents = Collections.emptyList();
        when(abonentRepository.findAll()).thenReturn(abonents);

        cdrService.generateCDRByMonth(1);

        verify(cdrRepositoryFile, times(0)).saveToFile(anyList());
        verify(cdrRepositoryH2, times(0)).saveAll(anyList());
    }

    @Test
    public void testGenerateCDRByMonth() throws IOException {
        List<Abonent> abonents = createTestAbonentList();
        when(abonentRepository.findAll()).thenReturn(abonents);

        cdrService.generateCDRByMonth(1);

        verify(cdrRepositoryFile).saveToFile(anyList());
        verify(cdrRepositoryH2).saveAll(anyList());
    }

    private List<Abonent> createTestAbonentList() {
        List<Abonent> abonentList = new ArrayList<>(List.of(
            new Abonent("123456789"),
            new Abonent("987654321")
        ));
        return abonentList;
    }
}
