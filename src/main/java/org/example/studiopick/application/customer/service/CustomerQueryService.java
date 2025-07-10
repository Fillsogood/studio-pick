package org.example.studiopick.application.customer.service;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.customer.dto.FrequentCustomerDto;
import org.example.studiopick.infrastructure.customer.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerQueryService {

    private final CustomerMapper customerMapper;

    public List<FrequentCustomerDto> getFrequentCustomers(String keyword) {
        return customerMapper.findFrequentCustomers(keyword);
    }
}
