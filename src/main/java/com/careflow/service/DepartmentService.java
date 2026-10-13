package com.careflow.service;

import com.careflow.dto.DepartmentDto;
import com.careflow.entity.Department;
import com.careflow.exception.ConflictException;
import com.careflow.exception.ResourceNotFoundException;
import com.careflow.repository.DepartmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentDto> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DepartmentDto getDepartmentById(Long id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));
        return mapToDto(department);
    }

    @Transactional
    public DepartmentDto createDepartment(DepartmentDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new ConflictException("Department with name '" + dto.getName() + "' already exists");
        }
        Department department = new Department(dto.getName(), dto.getDescription());
        Department saved = departmentRepository.save(department);
        return mapToDto(saved);
    }

    @Transactional
    public DepartmentDto updateDepartment(Long id, DepartmentDto dto) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + id));

        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        Department saved = departmentRepository.save(department);
        return mapToDto(saved);
    }

    public DepartmentDto mapToDto(Department d) {
        return new DepartmentDto(d.getId(), d.getName(), d.getDescription());
    }
}
