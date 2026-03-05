package br.edu.ufape.roomie.repository;

import br.edu.ufape.roomie.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    Address findByCity(String city);

    Address findByCep(String cep);

    Address findByState(String state);

    Address findByDistrict(String district);
}
