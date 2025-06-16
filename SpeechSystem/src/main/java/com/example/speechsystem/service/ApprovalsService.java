package com.example.speechsystem.service;

import com.example.speechsystem.pojo.Approval;
import com.example.speechsystem.pojo.PageBean;

public interface ApprovalsService {

    PageBean getPendingApprovals(Integer page, Integer pageSize);

    void approval(Approval approval);

    PageBean getApprovedApprovals(Integer page, Integer pageSize);
}
