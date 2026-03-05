package br.edu.ufape.roomie.controller;

import br.edu.ufape.roomie.dto.PropertyRequestDTO;
import br.edu.ufape.roomie.dto.PropertyResponseDTO;
import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.PropertyDetailView;
import br.edu.ufape.roomie.repository.PropertyRepository;
import br.edu.ufape.roomie.service.PropertyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
    public ResponseEntity<List<Property>> getAll(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String propertyType
    ) {
        String loc = (location != null && !location.isBlank()) ? location : "[ALL]";
        String dist = (district != null && !district.isBlank()) ? district : "[ALL]";
        double min = (minPrice != null) ? minPrice : -1.0;
        double max = (maxPrice != null) ? maxPrice : -1.0;
        String type = (propertyType != null && !propertyType.isBlank()) ? propertyType.toUpperCase() : "[ALL]";

        List<Property> properties = propertyRepository.findWithFilters(loc, dist, min, max, type);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getById(@PathVariable Long id) {
        return propertyRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/details")
    public ResponseEntity<List<PropertyDetailView>> getAllDetails() {
        return ResponseEntity.ok(propertyRepository.findAllDetails());
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<PropertyDetailView> getDetailById(@PathVariable Long id) {
        return propertyRepository.findDetailById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/meus")
    public ResponseEntity<List<PropertyDetailView>> getMyproperties(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        List<PropertyDetailView> properties = propertyRepository.findMyDetails(user.getEmail());
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/announcements/{id}")
    public ResponseEntity<PropertyResponseDTO> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyDetails(id));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<?> publishProperty(@PathVariable Long id, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        Property property = propertyRepository.findById(id).orElseThrow(() -> new RuntimeException("Imóvel não encontrado"));

        if (!property.getOwner().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Você não tem permissão para publicar este imóvel.");
        }

        if (property.getStatus() == PropertyStatus.ACTIVE) {
            return ResponseEntity.badRequest()
                    .body("Imóvel já está publicado.");
        }

        property.setStatus(PropertyStatus.ACTIVE);
        propertyRepository.save(property);

        return ResponseEntity.ok(property);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProperty(@PathVariable Long id) {
        try {
            propertyService.deleteProperty(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/draft")
    public ResponseEntity<?> setPropertyToDraft(@PathVariable Long id, Authentication authentication) {
        try {
            Property property = propertyService.setPropertyToDraft(id);
            return ResponseEntity.ok(property);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Property> updateProperty(
            @PathVariable Long id,
            @Valid @RequestPart("data") PropertyRequestDTO dto,
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        Property updated = propertyService.updateProperty(id, dto, photos);
        return ResponseEntity.ok(updated);
    }

}
