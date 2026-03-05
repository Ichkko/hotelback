package com.example.hotelback.controller;

import com.example.hotelback.model.Highlight;
import com.example.hotelback.service.HighlightService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/highlights")
public class HighlightController {

    private final HighlightService highlightService;

    public HighlightController(HighlightService highlightService) {
        this.highlightService = highlightService;
    }

    @PostMapping
    public ResponseEntity<Highlight> createHighlight(@RequestBody Highlight highlight) {
        return ResponseEntity.ok(highlightService.createHighlight(highlight));
    }

    @GetMapping
    public ResponseEntity<List<Highlight>> getAllHighlights() {
        return ResponseEntity.ok(highlightService.getAllHighlights());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Highlight> getHighlightById(@PathVariable Long id) {
        return highlightService.getHighlightById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Highlight> updateHighlight(@PathVariable Long id, @RequestBody Highlight highlight) {
        return ResponseEntity.ok(highlightService.updateHighlight(id, highlight));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteHighlight(@PathVariable Long id) {
        highlightService.deleteHighlightById(id);
        return ResponseEntity.ok("Highlight with ID " + id + " deleted successfully");
    }
}
