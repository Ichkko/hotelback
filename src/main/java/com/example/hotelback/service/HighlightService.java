package com.example.hotelback.service;

import com.example.hotelback.model.Highlight;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

public interface HighlightService {

    Highlight createHighlight(Highlight highlight, UserDetails principal);

    List<Highlight> getAllHighlights();

    Optional<Highlight> getHighlightById(Long id);

    Highlight updateHighlight(Long id, Highlight highlight, UserDetails principal);

    void deleteHighlightById(Long id, UserDetails principal);
}
