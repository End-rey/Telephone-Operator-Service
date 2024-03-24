package org.endrey.telephone.operator.repository;

import org.endrey.telephone.operator.entity.CDR;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CDRRepositoryH2 extends JpaRepository<CDR, Long> {
}
