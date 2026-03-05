package br.edu.ufape.roomie.service;

import br.edu.ufape.roomie.dto.PropertyRequestDTO;
import br.edu.ufape.roomie.dto.PropertyResponseDTO;
import br.edu.ufape.roomie.enums.PropertyStatus;
import br.edu.ufape.roomie.model.Address;
import br.edu.ufape.roomie.model.Property;
import br.edu.ufape.roomie.model.PropertyPhoto;
import br.edu.ufape.roomie.model.User;
import br.edu.ufape.roomie.projection.PropertyDetailView;
import br.edu.ufape.roomie.repository.PropertyPhotoRepository;
import br.edu.ufape.roomie.repository.PropertyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final FileStorageService fileStorageService;
        private final PropertyPhotoRepository propertyPhotoRepository;

    public PropertyService(PropertyRepository propertyRepository, FileStorageService fileStorageService, PropertyPhotoRepository propertyPhotoRepository) {
        this.propertyRepository = propertyRepository;
        this.fileStorageService = fileStorageService;
        this.propertyPhotoRepository = propertyPhotoRepository;
    }

    @Transactional
    public Property createProperty(PropertyRequestDTO dto, List<MultipartFile> photos) {
        User owner = this.getAuthenticatedUser();

        Property property = new Property();
        property.setOwner(owner);
        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setType(dto.getType());
        property.setPrice(dto.getPrice());
        property.setGender(dto.getGender());
        property.setAcceptAnimals(dto.getAcceptAnimals());
        property.setHasGarage(dto.getHasGarage());
        property.setAvailableVacancies(dto.getAvailableVacancies());
        property.setStatus(PropertyStatus.DRAFT);

        Address address = new Address();
        address.setStreet(dto.getAddress().getStreet());
        address.setDistrict(dto.getAddress().getDistrict());
        address.setNumber(dto.getAddress().getNumber());
        address.setCity(dto.getAddress().getCity());
        address.setState(dto.getAddress().getState());
        address.setCep(dto.getAddress().getCep());

        address.setProperty(property);
        property.setAddress(address);

        List<PropertyPhoto> propertyPhotos = new ArrayList<>();
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile photo : photos) {
                String fileName = fileStorageService.storeFile(photo);

                PropertyPhoto propertyPhoto = new PropertyPhoto();
                propertyPhoto.setPath("/images/" + fileName);
                propertyPhoto.setProperty(property);

                propertyPhotos.add(propertyPhoto);
            }
        }
        property.setPhotos(propertyPhotos);

        return propertyRepository.save(property);
    }

    public Property publishProperty(Long propertyId) {
        User user = getAuthenticatedUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (!property.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para publicar este imóvel.");
        }

        property.setStatus(PropertyStatus.ACTIVE);
        return propertyRepository.save(property);
    }

    @Transactional
    public void deleteProperty(Long propertyId) {
        User user = getAuthenticatedUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (!property.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para excluir este imóvel.");
        }

        propertyRepository.delete(property);
    }

    @Transactional
    public Property setPropertyToDraft(Long propertyId) {
        User user = getAuthenticatedUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (!property.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este imóvel.");
        }

        property.setStatus(PropertyStatus.DRAFT);
        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(Long propertyId, PropertyRequestDTO dto, List<MultipartFile> photos) {
        User user = getAuthenticatedUser();

        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));

        if (!property.getOwner().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não tem permissão para editar este imóvel.");
        }

        property.setTitle(dto.getTitle());
        property.setDescription(dto.getDescription());
        property.setType(dto.getType());
        property.setPrice(dto.getPrice());
        property.setGender(dto.getGender());
        property.setAcceptAnimals(dto.getAcceptAnimals());
        property.setHasGarage(dto.getHasGarage());
        property.setAvailableVacancies(dto.getAvailableVacancies());
        property.setStatus(PropertyStatus.DRAFT);

        Address address = property.getAddress();
        address.setStreet(dto.getAddress().getStreet());
        address.setDistrict(dto.getAddress().getDistrict());
        address.setNumber(dto.getAddress().getNumber());
        address.setCity(dto.getAddress().getCity());
        address.setState(dto.getAddress().getState());
        address.setCep(dto.getAddress().getCep());

        if (photos != null && !photos.isEmpty()) {
            property.getPhotos().clear();
            for (MultipartFile photo : photos) {
                String fileName = fileStorageService.storeFile(photo);
                PropertyPhoto propertyPhoto = new PropertyPhoto();
                propertyPhoto.setPath("/images/" + fileName);
                propertyPhoto.setProperty(property);
                property.getPhotos().add(propertyPhoto);
            }
        }

        return propertyRepository.save(property);
    }

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário não autenticado.");
        }

        return (User) principal;
    }

    @Transactional(readOnly = true)
    public PropertyResponseDTO getPropertyDetails(Long id){
        getAuthenticatedUser();

        PropertyDetailView details = propertyRepository.findDetailById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));
        
        if(!"ACTIVE".equals(details.getStatus())){
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Anúncio não está disponivel");
        }
        List<String> photos = propertyPhotoRepository.findPhotosByPropertyId(id);

        return new PropertyResponseDTO(details, photos);
    }

}
