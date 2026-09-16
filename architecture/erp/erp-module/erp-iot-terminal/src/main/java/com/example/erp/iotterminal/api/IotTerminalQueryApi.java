package com.example.erp.iotterminal.api;

import com.example.erp.iotterminal.api.dto.TerminalDto;

public interface IotTerminalQueryApi {

    TerminalDto findById(String id);
}