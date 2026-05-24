package com.example.hotelback.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotelReportSummaryResponse {

    private Long hotelId;

    // Захиалгын тоо
    private long totalBookings;
    private long newBookings;
    private long confirmedBookings;
    private long paidBookings;
    private long cancelledBookings;

    // Санхүүгийн мэдээлэл
    private BigDecimal totalRevenue;       // PAID захиалгын нийт үнэ
    private BigDecimal totalServiceFee;    // нийт үйлчилгээний хөлс
    private BigDecimal totalPaymentsReceived; // бодитоор авсан төлбөр

    // Өрөөний ачааллал
    private long totalRooms;
    private long occupiedRooms; // өнөөдөр захиалгатай
}
