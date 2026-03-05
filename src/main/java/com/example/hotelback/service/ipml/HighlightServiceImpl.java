package com.example.hotelback.service.ipml;

import com.example.hotelback.model.Highlight;
import com.example.hotelback.repository.HighlightRepository;
import com.example.hotelback.service.HighlightService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class HighlightServiceImpl implements HighlightService {

    private final HighlightRepository highlightRepository;

    public HighlightServiceImpl(HighlightRepository highlightRepository) {
        this.highlightRepository = highlightRepository;
    }

    @Override
    public Highlight createHighlight(Highlight highlight) {
        return highlightRepository.save(highlight);
    }

    @Override
    public List<Highlight> getAllHighlights() {
        return highlightRepository.findAll();
    }

    @Override
    public Optional<Highlight> getHighlightById(Long id) {
        return highlightRepository.findById(id);
    }

    @Override
    public Highlight updateHighlight(Long id, Highlight highlight) {
        Highlight existing = highlightRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Highlight not found"));
        BeanUtils.copyProperties(highlight, existing, "id", "createdAt", "updatedAt");
        return highlightRepository.save(existing);
    }

    @Override
    public void deleteHighlightById(Long id) {
        highlightRepository.deleteById(id);
    }
}
