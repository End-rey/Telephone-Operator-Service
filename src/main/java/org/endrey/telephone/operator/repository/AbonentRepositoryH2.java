package org.endrey.telephone.operator.repository;

import org.endrey.telephone.operator.entity.Abonent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbonentRepositoryH2 extends JpaRepository<Abonent, Long> {
    Abonent findByPhoneNumber(String phoneNumber);
}
