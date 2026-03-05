package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.PropertyPhoto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyPhotoRepository extends JpaRepository<PropertyPhoto, Long> {
    @Query("""
        SELECT p.path
        FROM PropertyPhoto p
        WHERE p.property.id = :id
    """)
    List<String> findPhotosByPropertyId(@Param("id") Long id);
}

