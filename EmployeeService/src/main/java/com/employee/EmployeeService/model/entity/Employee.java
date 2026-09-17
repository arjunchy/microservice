package com.employee.EmployeeService.model.entity;

import com.employee.EmployeeService.model.EmployeeStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "employee")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String empName;

    @Email
    @Column(unique = true, nullable = false)
    private String empEmail;

    @NotBlank
    private String designation;

    @NotBlank
    private String empDepartment;

    @NotBlank
    private String companyName;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

}
