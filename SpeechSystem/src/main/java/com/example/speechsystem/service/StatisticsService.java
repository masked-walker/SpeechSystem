package com.example.speechsystem.service;

import com.example.speechsystem.pojo.DTO.*;

import java.util.Date;
import java.util.List;

public interface StatisticsService {
    List<RoomUsageDTO> getRoomUsageStatistics(Date startDate, Date endDate);
    
    List<BookingStatusDTO> getBookingStatusStatistics(Date startDate, Date endDate);
    
    List<UserRankingDTO> getUserBookingRanking(Date startDate, Date endDate, Integer limit);
    
    List<MeetingDurationDTO> getMeetingDurationStatistics(Date startDate, Date endDate);
    
    AllStatisticsDTO getAllStatistics(Date startDate, Date endDate);
}