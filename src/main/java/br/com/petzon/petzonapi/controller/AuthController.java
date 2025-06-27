package br.com.petzon.petzonapi.controller;

import br.com.petzon.petzonapi.dto.LoginDto;
import br.com.petzon.petzonapi.dto.TokenDto;
import br.com.petzon.petzonapi.dto.UsuarioDto;
import br.com.petzon.petzonapi.dto.UsuarioLogadoDto;
import br.com.petzon.petzonapi.exception.RegraDeNegocioException;
import br.com.petzon.petzonapi.entity.Usuario;
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
    public ResponseEntity<TokenDto> login(@RequestBody @Valid LoginDto loginDto) {
        UsernamePasswordAuthenticationToken userAuth =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getSenha());

        Authentication authentication = authenticationManager.authenticate(userAuth);
        Object principal = authentication.getPrincipal();
        Usuario usuarioAutenticado = (Usuario) principal;

        String token = tokenService.generateToken(usuarioAutenticado);

        return ResponseEntity.ok(new TokenDto(token));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UsuarioDto usuarioDto) {
        try {
            usuarioService.criarUsuario(usuarioDto);
            return ResponseEntity.ok().build();
        } catch (RegraDeNegocioException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/usuario-logado")
    public ResponseEntity<UsuarioLogadoDto> getLoggedUser() throws RegraDeNegocioException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Integer idUsuario = Integer.parseInt((String) principal);

        UsuarioLogadoDto usuarioLogadoDto = usuarioService.getLoggedUser(idUsuario);
        return ResponseEntity.ok(usuarioLogadoDto);
    }
}