package org.endrey.telephone.operator.dataLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.endrey.telephone.operator.entity.Abonent;
import org.endrey.telephone.operator.repository.AbonentRepositoryH2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DataLoader {

    private final ResourceLoader resourceLoader;
    
    @Value("${file.path.abonents}")
    private String filePathAbonents;

    @Autowired
    private AbonentRepositoryH2 abonentRepository;

    public DataLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadAbonents() {
        if (abonentRepository.count() == 0) {
            abonentRepository.saveAll(readPhoneNumbersFromFile(filePathAbonents));
        }
    }

    private Iterable<Abonent> readPhoneNumbersFromFile(String filePath) {
        List<Abonent> phoneNumbers = new ArrayList<>();
        Resource resource = resourceLoader.getResource("classpath:" + filePath);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
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
