package com.ticketrush.backend.service.impl;

import com.ticketrush.backend.dto.TheaterResponse;
import com.ticketrush.backend.entity.Theater;
import com.ticketrush.backend.repository.TheaterRepository;
import com.ticketrush.backend.service.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TheaterServiceImpl implements TheaterService {

    private final TheaterRepository theaterRepository;

    @Override
    public List<TheaterResponse> getAllTheaters() {
        return theaterRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    private TheaterResponse toResponse(Theater theater) {
        return new TheaterResponse(
                theater.getId(),
                theater.getName(),
                theater.getLocation(),
                theater.getCapacity()
        );
    }
}
