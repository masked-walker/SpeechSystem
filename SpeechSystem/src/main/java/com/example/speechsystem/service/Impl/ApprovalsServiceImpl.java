package com.example.speechsystem.service.Impl;

import com.example.speechsystem.annotation.LogOperation;
import com.example.speechsystem.exception.BusinessException;
import com.example.speechsystem.mapper.ApprovalsMapper;
import com.example.speechsystem.mapper.BookingsMapper;
import com.example.speechsystem.mapper.SystemConfigMapper;
import com.example.speechsystem.pojo.Approval;
import com.example.speechsystem.pojo.Booking;
import com.example.speechsystem.pojo.PageBean;
import com.example.speechsystem.pojo.SystemConfig;
import com.example.speechsystem.pojo.VO.ApprovalVO;
import com.example.speechsystem.service.ApprovalsService;
import com.example.speechsystem.service.NotificationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ApprovalsServiceImpl implements ApprovalsService {
    @Autowired
    private ApprovalsMapper approvalsMapper;
    @Autowired
    private BookingsMapper bookingsMapper;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SystemConfigMapper systemConfigMapper;

    /**
     * 获取待审批列表
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageBean getPendingApprovals(Integer page, Integer pageSize) {
        // 检查是否启用了审批功能
        if (!isApprovalRequired()) {
            // 若未启用审批功能，返回空列表
            return new PageBean(0L, new ArrayList<>());
        }
        
        PageHelper.startPage(page, pageSize);
        List<ApprovalVO> list = approvalsMapper.getPendingApprovals();
        Page<ApprovalVO> p = (Page<ApprovalVO>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }

    /**
     * 审批预约
     * @param approval
     */
    @Override
    @LogOperation(module = "预约管理", operation = "审批预约", description = "审批预约")
    public void approval(Approval approval) {
        // 检查是否启用了审批功能
        if (!isApprovalRequired()) {
            throw new BusinessException("系统当前未启用审批功能，无法执行审批操作");
        }
        
        approvalsMapper.approval(approval);
        Long bookingId = approvalsMapper.getBookingId(approval.getId());
        bookingsMapper.bookingStatusChange(approval.getStatus(), bookingId);
        Booking booking = bookingsMapper.getById(Math.toIntExact(bookingId));
        Integer oldStatus = approval.getStatus();
        notificationService.sendStatusChangeNotification(booking, oldStatus, approval.getStatus());
    }

    /**
     * 获取已审批列表
     * @param page
     * @param pageSize
     * @return
     */
    @Override
    public PageBean getApprovedApprovals(Integer page, Integer pageSize) {
        // 即使未启用审批功能，也允许查看历史审批记录
        PageHelper.startPage(page, pageSize);
        List<ApprovalVO> list = approvalsMapper.getApprovedApprovals();
        Page<ApprovalVO> p = (Page<ApprovalVO>) list;

        PageBean pageBean = new PageBean(p.getTotal(), p.getResult());
        return pageBean;
    }
    
    /**
     * 检查系统是否启用了审批功能
     * @return 是否启用审批
     */
    private boolean isApprovalRequired() {
        SystemConfig approvalConfig = systemConfigMapper.getByConfigKey("APPROVAL_REQUIRED");
        return approvalConfig != null && 
               ("true".equalsIgnoreCase(approvalConfig.getConfigValue()) || 
                "1".equals(approvalConfig.getConfigValue()));
    }
}
