package com.tuempresa.cotizador.web.dto.admin;

import com.tuempresa.cotizador.model.enums.SubscriptionStatus;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class SubscriptionUpdateDTO {
    private String planType;
    private SubscriptionStatus newStatus;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate newEndDate;
}