package org.endrey.telephone.operator.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class UDR {
    private String msisdn;
    private TotalTime incomingCall;
    private TotalTime outcomingCall;
}
