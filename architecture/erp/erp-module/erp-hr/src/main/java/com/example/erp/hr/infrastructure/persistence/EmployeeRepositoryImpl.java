package com.example.erp.hr.infrastructure.persistence;

import com.example.erp.hr.domain.model.Employee;
import com.example.erp.hr.domain.repository.EmployeeRepository;
import com.example.erp.shared.IdGenerator;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class EmployeeRepositoryImpl implements EmployeeRepository {

    private final EmployeeJpaRepository jpaRepository;

    public EmployeeRepositoryImpl(EmployeeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Employee save(Employee aggregate) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(aggregate.id() == null ? IdGenerator.next() : aggregate.id());
        entity.setName(aggregate.name());
        EmployeeEntity saved = jpaRepository.save(entity);
        return new Employee(saved.getId(), saved.getName());
    }

    @Override
    public Optional<Employee> findById(String id) {
        return jpaRepository.findById(id).map(e -> new Employee(e.getId(), e.getName()));
    }
}