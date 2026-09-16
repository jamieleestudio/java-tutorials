package com.example.erp.moraleducation.api;

import com.example.erp.moraleducation.api.dto.MoralActivityDto;

public interface MoralEducationApi {

    MoralActivityDto findById(String id);
}