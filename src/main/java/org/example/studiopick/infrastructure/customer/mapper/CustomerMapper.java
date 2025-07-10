package org.example.studiopick.infrastructure.customer.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.example.studiopick.application.customer.dto.FrequentCustomerDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CustomerMapper {

    List<FrequentCustomerDto> findFrequentCustomers(@Param("keyword") String keyword);

}
