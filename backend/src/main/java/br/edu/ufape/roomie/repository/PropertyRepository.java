package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("SELECT p FROM Property p LEFT JOIN p.address a " +
            "WHERE (:location = '[ALL]' OR LOWER(a.city) LIKE LOWER(CONCAT('%', :location, '%'))) " +
            "AND (:district = '[ALL]' OR LOWER(a.district) LIKE LOWER(CONCAT('%', :district, '%'))) " +
            "AND (:minPrice < 0 OR p.price >= :minPrice) " +
            "AND (:maxPrice < 0 OR p.price <= :maxPrice) " +
            "AND (:type = '[ALL]' OR CAST(p.type AS string) = :type) ")
    List<Property> findWithFilters(
        @Param("location") String location,
        @Param("district") String district,
        @Param("minPrice") double minPrice,
        @Param("maxPrice") double maxPrice,
        @Param("type") String type
    );
}
