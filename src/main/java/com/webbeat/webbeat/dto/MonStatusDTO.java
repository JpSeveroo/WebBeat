package com.webbeat.webbeat.dto;

import com.webbeat.webbeat.model.Monitored;

public record MonStatusDTO(

        Monitored monitored,
        Integer status

) {}
