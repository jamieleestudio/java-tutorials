package com.example.erp.dormitory.api;

import com.example.erp.dormitory.api.dto.DormitoryDto;

public interface DormitoryApi {

    DormitoryDto findById(String id);
}