package org.example.studiopick.web.customer;

import lombok.RequiredArgsConstructor;
import org.example.studiopick.application.customer.dto.FrequentCustomerDto;
import org.example.studiopick.application.customer.service.CustomerQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerQueryService customerQueryService;

    @GetMapping("/frequent")
    public List<FrequentCustomerDto> getFrequentCustomers(
            @RequestParam(required = false) String keyword
    ) {
        return customerQueryService.getFrequentCustomers(keyword);
    }
}
