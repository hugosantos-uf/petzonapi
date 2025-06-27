package br.com.petzon.petzonapi.security;

import br.com.petzon.petzonapi.entity.Cargo;
import br.com.petzon.petzonapi.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    private static final String TOKEN_PREFIX = "Bearer";
    private static final String CARGOS_CLAIM = "cargos";

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private String expiration;

    public String generateToken(Usuario usuario) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + Long.parseLong(expiration));

        List<String> cargos = usuario.getCargos().stream()
                .map(Cargo::getAuthority)
                .collect(Collectors.toList());

        return TOKEN_PREFIX + " " +
                Jwts.builder()
                        .setIssuer("petzon-api")
                        .claim(Claims.ID, usuario.getIdUsuario().toString())
                        .claim(CARGOS_CLAIM, cargos)
                        .setIssuedAt(now)
                        .setExpiration(exp)
                        .signWith(SignatureAlgorithm.HS256, secret)
                        .compact();
    }

    public String getTokenFromHeader(String header) {
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return header.replace(TOKEN_PREFIX, "").trim();
    }

    public UsernamePasswordAuthenticationToken isValid(String token) {
        if (token != null) {
            try {
                Claims body = Jwts.parser()
                        .setSigningKey(secret)
                        .parseClaimsJws(token.replace(TOKEN_PREFIX, "").trim())
                        .getBody();
                String user = body.get(Claims.ID, String.class);
                if (user != null) {
                    List<String> cargos = body.get(CARGOS_CLAIM, List.class);
                    List<SimpleGrantedAuthority> authorities = cargos.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());

                    return new UsernamePasswordAuthenticationToken(user, null, authorities);
                }
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}