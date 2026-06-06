package br.com.sentinela.service;

import br.com.sentinela.domain.dto.request.LoginRequest;
import br.com.sentinela.domain.dto.request.RegisterRequest;
import br.com.sentinela.domain.dto.response.TokenResponse;
import br.com.sentinela.domain.model.Usuario;
import br.com.sentinela.repository.UsuarioRepository;
import br.com.sentinela.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authManager;

    public TokenResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail()))
            throw new RuntimeException("Email já cadastrado");

        Usuario usuario = Usuario.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .build();

        usuarioRepository.save(usuario);
        String token = tokenProvider.generateToken(usuario.getEmail());
        return new TokenResponse(token, "Bearer", usuario.getEmail(), usuario.getNome());
    }

    public TokenResponse login(LoginRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha()));
        String token = tokenProvider.generateToken(request.getEmail());
        Optional<Usuario> usuario = usuarioRepository.findByEmail(request.getEmail());
        var nome = usuario.get().getNome();
        return new TokenResponse(token, "Bearer", request.getEmail(),nome);
    }
}