package com.example.erp.dormitory.api;

import com.example.erp.dormitory.api.dto.DormitoryDto;

public interface DormitoryQueryApi {

    DormitoryDto findById(String id);
}