package com.ticketrush.backend.service;

import com.ticketrush.backend.dto.TheaterResponse;

import java.util.List;

public interface TheaterService {

    List<TheaterResponse> getAllTheaters();
}
