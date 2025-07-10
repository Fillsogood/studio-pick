package org.example.studiopick.application.customer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrequentCustomerDto {
    private Long userId;
    private String name;
    private String phone;
    private LocalDate lastVisitDate;
    private Integer visitCount;
}
