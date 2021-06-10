package com.example.desafiodigital.controllers;

import com.example.desafiodigital.domain.Pauta;
import com.example.desafiodigital.services.PautaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pauta")
public class PautaController {

    private final PautaService pautaService;

    public PautaController(PautaService pautaService) {
        this.pautaService = pautaService;
    }

    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody Pauta pauta){
        pautaService.save(pauta);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping
    public List<Pauta> getAllPautas(){
        return pautaService.findAll();
    }


}
