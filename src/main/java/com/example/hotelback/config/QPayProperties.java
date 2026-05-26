package com.example.hotelback.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qpay")
public record QPayProperties(
        String baseUrl,
        String username,
        String password,
        String invoiceCode,
        String callbackUrl,
        String senderBranchCode,
        String senderStaffCode
) {
}
