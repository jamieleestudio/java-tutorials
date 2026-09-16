package com.example.erp.iotterminal.api;

import com.example.erp.iotterminal.api.dto.TerminalDto;

public interface IotTerminalQueryService {

    TerminalDto findById(String id);
}