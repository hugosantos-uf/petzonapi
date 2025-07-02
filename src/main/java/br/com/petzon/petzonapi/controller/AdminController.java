package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.UsuarioResponse;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PutMapping("/usuarios/{id}/promover-ong")
    public ResponseEntity<UsuarioResponse> promoverParaOng(@PathVariable int id) throws RegraDeNegocioException {
        return ResponseEntity.ok(usuarioService.promoverParaOng(id));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<String> desativarUsuario(@PathVariable int id) throws RegraDeNegocioException {
        return ResponseEntity.ok(usuarioService.desativarUsuario(id));
    }
}