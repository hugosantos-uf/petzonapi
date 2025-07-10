package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.UsuarioResponse;
import br.com.petzon.petzonapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UsuarioService usuarioService;

    @GetMapping("/usuarios")
    public ResponseEntity<Page<UsuarioResponse>> listarUsuarios(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarUsuarios(pageable));
    }

    @GetMapping("/usuarios/nome")
    public ResponseEntity<Page<UsuarioResponse>> buscarUsuarioPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.findByName(nome,pageable));
    }

    @PutMapping("/usuarios/{id}/promover-ong")
    public ResponseEntity<UsuarioResponse> promoverParaOng(@PathVariable int id){
        return ResponseEntity.ok(usuarioService.promoverParaOng(id));
    }

    @DeleteMapping("/usuarios/{id}")
    public ResponseEntity<String> desativarUsuario(@PathVariable int id){
        return ResponseEntity.ok(usuarioService.desativarUsuario(id));
    }
}