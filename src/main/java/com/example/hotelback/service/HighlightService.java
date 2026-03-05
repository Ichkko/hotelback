package com.example.hotelback.service;

import com.example.hotelback.model.Highlight;

import java.util.List;
import java.util.Optional;

public interface HighlightService {

    Highlight createHighlight(Highlight highlight);

    List<Highlight> getAllHighlights();

    Optional<Highlight> getHighlightById(Long id);

    Highlight updateHighlight(Long id, Highlight highlight);

    void deleteHighlightById(Long id);
}
