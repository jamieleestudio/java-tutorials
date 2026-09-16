package com.example.erp.dormitory.api;

import com.example.erp.dormitory.api.dto.DormitoryDto;

public interface DormitoryQueryService {

    DormitoryDto findById(String id);
}