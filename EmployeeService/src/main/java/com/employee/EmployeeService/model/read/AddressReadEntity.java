package com.employee.EmployeeService.model.read;

import com.employee.EmployeeService.model.entity.AddressType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "address", schema = "address_db")
@Getter
@Setter
@NoArgsConstructor
public class AddressReadEntity {

    @Id
    private Long id;

    private Long employeeId;

    private String city;

    private String country;

    private String zipCode;

    @Enumerated(EnumType.STRING)
    private AddressType addressType;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}