package br.com.petzon.petzonapi.repository;

import br.com.petzon.petzonapi.entity.Pet;
import br.com.petzon.petzonapi.entity.PetType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {

    List<Pet> findByTipo(PetType tipo);
}