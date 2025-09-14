package com.kushagra.movieflix.controller;

import com.kushagra.movieflix.dto.CustomResponse;
import com.kushagra.movieflix.service.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
@Slf4j
public class StatsController {

    private final StatsService statsService;

    @GetMapping
    public CustomResponse getStats() {
        log.info("Entered into method getStats in StatsController");
        return statsService.getStats();
    }
}

