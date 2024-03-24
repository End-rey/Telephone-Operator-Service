package org.endrey.telephone.operator.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "abonents")
public class Abonent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String phoneNumber;

    @OneToMany(mappedBy = "phoneNumber", fetch = FetchType.EAGER)
    List<CDR> cdrList = new ArrayList<>();

    public Abonent(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
