package org.endrey.telephone.operator.entity;

import java.time.LocalDateTime;

import org.endrey.telephone.operator.enums.CallType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class CDR {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private CallType callType;

    @ManyToOne
    @JoinColumn(name = "phone_number", nullable = false)
    private Abonent phoneNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
