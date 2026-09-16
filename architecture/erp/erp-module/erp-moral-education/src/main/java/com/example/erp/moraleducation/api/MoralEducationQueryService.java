package com.example.erp.moraleducation.api;

import com.example.erp.moraleducation.api.dto.MoralActivityDto;

public interface MoralEducationQueryService {

    MoralActivityDto findById(String id);
}