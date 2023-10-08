package com.capstone.project.samplePortfolio.entity;

import com.fasterxml.jackson.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portfoliotable")
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Portfolio {
    @Transient
    public static final String SEQUENCE_NAME = "portfolio _sequence";
    @Id
    private long portfolioId;
    private String portfolioName;
    private String investmentAgenda;
    private Date createdAt;
    private Date updatedAt;
//    private Double balance;
//    private Double totalReturn;
    private Long userID;
    private List<Stock> stocks;
}
