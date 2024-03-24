package org.endrey.telephone.operator.dataLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.endrey.telephone.operator.entity.Abonent;
import org.endrey.telephone.operator.repository.AbonentRepositoryH2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {
    
    @Value("${file.path.abonents}")
    private String filePathAbonents;

    @Autowired
    private AbonentRepositoryH2 abonentRepository;

    @PostConstruct
    public void loadAbonents() {
        if (abonentRepository.count() == 0) {
            abonentRepository.saveAll(readPhoneNumbersFromFile(filePathAbonents));
        }
    }

    private Iterable<Abonent> readPhoneNumbersFromFile(String filePath) {
        List<Abonent> phoneNumbers = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                phoneNumbers.add(new Abonent(line.trim()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return phoneNumbers;
    }
}
