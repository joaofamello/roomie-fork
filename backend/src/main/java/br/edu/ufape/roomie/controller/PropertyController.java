package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.PropertyRequestDTO;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final PropertyRepository propertyRepository;

    public PropertyController(PropertyService propertyService, PropertyRepository propertyRepository) {
        this.propertyService = propertyService;
        this.propertyRepository = propertyRepository;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Property> store(
            @Valid @RequestPart("data") PropertyRequestDTO dto,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        Property createdProperty = propertyService.createProperty(dto, photos);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProperty);
    }

    @GetMapping
    public ResponseEntity<List<Property>> getAll() {
        List<Property> properties = propertyRepository.findAll();
        return ResponseEntity.ok(properties);
    }

}
