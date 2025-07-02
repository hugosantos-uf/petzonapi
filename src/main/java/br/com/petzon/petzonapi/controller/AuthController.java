package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.LoginRequest;
import br.com.petzon.petzonapi.dto.LoginResponse;
import br.com.petzon.petzonapi.dto.UsuarioRequest;
import br.com.petzon.petzonapi.dto.UsuarioResponse;
import br.com.petzon.petzonapi.entity.Usuario;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.security.TokenService;
import br.com.petzon.petzonapi.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken userAuth =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getSenha());

        Authentication authentication = authenticationManager.authenticate(userAuth);
        Object principal = authentication.getPrincipal();
        Usuario usuarioAutenticado = (Usuario) principal;

        String token = tokenService.generateToken(usuarioAutenticado);

        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@RequestBody @Valid UsuarioRequest usuarioRequest) {
            UsuarioResponse usuarioCriado = usuarioService.criarUsuario(usuarioRequest);
            return new ResponseEntity<>(usuarioCriado, HttpStatus.CREATED);
    }

    @GetMapping("/usuario-logado")
    public ResponseEntity<UsuarioResponse> getLoggedUser() throws RegraDeNegocioException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer idUsuario = Integer.parseInt((String) principal);

        UsuarioResponse UsuarioResponse = usuarioService.getLoggedUser(idUsuario);
        return ResponseEntity.ok(UsuarioResponse);
    }
}