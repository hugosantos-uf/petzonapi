package br.com.petzon.petzonapi.repository;

import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {

    Page<Pet> findByTipo(PetType tipo, Pageable pageable);
}