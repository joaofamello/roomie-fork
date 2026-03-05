package br.edu.ufape.roomie.dto;

import br.edu.ufape.roomie.projection.PropertyDetailView;

import java.util.List;

public class PropertyResponseDTO {
    private PropertyDetailView details; 
    private List<String> photos; 

    public PropertyResponseDTO(PropertyDetailView details, List<String> photos) {
        this.details = details;
        this.photos = photos; 
    }

    public PropertyDetailView getDetails(){
        return details;
    }

    public List<String> getPhotos() {
        return photos;
    }
}
