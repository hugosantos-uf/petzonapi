package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.UsuarioDto;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioDto>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PutMapping("/usuarios/{id}/promover-ong")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioDto> promoverParaOng(@PathVariable Integer id) throws RegraDeNegocioException {
        return ResponseEntity.ok(usuarioService.promoverParaOng(id));
    }
}