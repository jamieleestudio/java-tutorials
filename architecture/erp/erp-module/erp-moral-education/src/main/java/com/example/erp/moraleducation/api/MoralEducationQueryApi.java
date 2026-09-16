package com.example.erp.moraleducation.api;

import com.example.erp.moraleducation.api.dto.MoralActivityDto;

public interface MoralEducationQueryApi {

    MoralActivityDto findById(String id);
}