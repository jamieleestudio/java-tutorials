package com.example.erp.hr.domain.repository;

import com.example.erp.hr.domain.model.Employee;

import java.util.Optional;

public interface EmployeeRepository {

    Employee save(Employee aggregate);

    Optional<Employee> findById(String id);
}