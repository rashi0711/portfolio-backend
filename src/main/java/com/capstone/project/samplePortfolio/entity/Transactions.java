package com.capstone.project.samplePortfolio.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class Transactions {
    @Id
    private String type;
    private Date createdAt;
    private int quantity;
    private Double amount;
}
