package com.example.speechsystem.service;

import com.example.speechsystem.pojo.Booking;
import com.example.speechsystem.pojo.PageBean;

import java.time.LocalDate;

public interface BookingsService {
    PageBean getBookings(Integer page, Integer pageSize, Integer roomId, Integer userId, Integer status, LocalDate begin, LocalDate end);

    Booking getById(Integer id);

    void addBooking(Booking booking);

    void updateBooking(Booking booking);

    void cancelBooking(Integer id);
}
