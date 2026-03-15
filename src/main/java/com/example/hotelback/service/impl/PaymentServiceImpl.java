package com.example.hotelback.service.impl;

import com.example.hotelback.exception.ResourceNotFoundException;
import com.example.hotelback.model.Booking;
import com.example.hotelback.model.BookingStatus;
import com.example.hotelback.model.Payment;
import com.example.hotelback.model.PaymentStatus;
import com.example.hotelback.repository.BookingRepository;
import com.example.hotelback.repository.PaymentRepository;
import com.example.hotelback.service.PaymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              BookingRepository bookingRepository) {
        this.paymentRepository = paymentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public Payment createPayment(Payment payment) {
        if (payment.getBooking() == null || payment.getBooking().getId() == null) {
            throw new IllegalArgumentException("Төлбөрийн booking мэдээлэл дутуу байна");
        }
        if (payment.getAmount() == null || payment.getAmount().signum() <= 0) {
            throw new IllegalArgumentException("Төлбөрийн дүн буруу байна");
        }

        // Booking-ийг lock-тойгоор унших
        Booking booking = bookingRepository.findByIdForUpdate(payment.getBooking().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Захиалга олдсонгүй: ID=" + payment.getBooking().getId()));

        // Зөвхөн CONFIRMED төлөвтэй booking дээр төлбөр хийх
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new IllegalStateException("Зөвхөн CONFIRMED төлөвтэй захиалгад төлбөр хийх боломжтой");
        }

        // Нийт төлөгдөх ёстой үнийг энгийнээр: room.price * хоног
        long nights = java.time.temporal.ChronoUnit.DAYS.between(
                booking.getCheckinDate(),
                booking.getCheckoutDate()
        );
        if (nights <= 0) {
            throw new IllegalStateException("Захиалгын огноо буруу тул төлбөр тооцоолох боломжгүй");
        }
        java.math.BigDecimal expectedTotal =
                java.math.BigDecimal.valueOf(booking.getRoom().getPrice())
                        .multiply(java.math.BigDecimal.valueOf(nights));

        // Өмнө хийгдсэн амжилттай төлбөрүүдийг шалгах (SUCCESS статустай төлбөрүүдийг л тооцно)
        java.math.BigDecimal alreadyPaid = paymentRepository.findByBooking_Id(booking.getId())
                .stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        java.math.BigDecimal newTotalPaid = alreadyPaid.add(payment.getAmount());
        if (newTotalPaid.compareTo(expectedTotal) > 0) {
            throw new IllegalStateException("Нийт төлбөрөөс илүү дүнгээр төлж байна");
        }

        // Төлбөрийн төлөв, огноо
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentDate(java.time.Instant.now());
        payment.setBooking(booking);

        Payment saved = paymentRepository.save(payment);

        // Хэрэв бүрэн төлөгдсөн бол booking төлөвийг PAID болгох
        if (newTotalPaid.compareTo(expectedTotal) == 0) {
            booking.setStatus(BookingStatus.PAID);
            bookingRepository.save(booking);
        }

        return saved;
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    @Override
    public Payment updatePayment(Long id, Payment payment) {
        Payment existing = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        BeanUtils.copyProperties(payment, existing, "id", "createdAt", "updatedAt");
        return paymentRepository.save(existing);
    }

    @Override
    public void deletePaymentById(Long id) {
        paymentRepository.deleteById(id);
    }
}
