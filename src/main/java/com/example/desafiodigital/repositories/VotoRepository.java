package com.example.desafiodigital.repositories;

import com.example.desafiodigital.domain.Voto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VotoRepository extends JpaRepository<Voto, Integer> {

    Optional<Voto> findByCpfAndPautaId(String cpf, Integer id);

    Optional<List<Voto>> findByPautaId(Integer id);
}
