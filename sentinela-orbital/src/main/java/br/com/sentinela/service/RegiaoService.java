package br.com.sentinela.service;

import br.com.sentinela.domain.dto.request.RegiaoRequest;
import br.com.sentinela.domain.dto.response.RegiaoResponse;
import br.com.sentinela.domain.model.Regiao;
import br.com.sentinela.domain.model.Usuario;
import br.com.sentinela.exception.RegiaoNotFoundException;
import br.com.sentinela.repository.RegiaoRepository;
import br.com.sentinela.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegiaoService {

    private final RegiaoRepository regiaoRepository;
    private final UsuarioRepository usuarioRepository;

    public List<RegiaoResponse> listar(String email) {
        return regiaoRepository.findByUsuarioId(getUsuario(email).getId())
                .stream().map(this::toResponse).toList();
    }

    public RegiaoResponse buscarPorId(Long id) {
        return toResponse(regiaoRepository.findById(id)
                .orElseThrow(() -> new RegiaoNotFoundException(id)));
    }

    public RegiaoResponse buscarMaisProxima(Double lat, Double lng, String email) {
        return regiaoRepository.findByUsuarioId(getUsuario(email).getId())
                .stream()
                .min(Comparator.comparingDouble(r ->
                        Math.sqrt(Math.pow(r.getLatitude() - lat, 2) + Math.pow(r.getLongitude() - lng, 2))))
                .map(this::toResponse)
                .orElseThrow(() -> new RuntimeException("Nenhuma região cadastrada para este usuário."));
    }

    public RegiaoResponse criar(RegiaoRequest request, String email) {
        Regiao regiao = Regiao.builder()
                .nome(request.getNome())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .descricao(request.getDescricao())
                .usuario(getUsuario(email))
                .build();
        return toResponse(regiaoRepository.save(regiao));
    }

    public RegiaoResponse atualizar(Long id, RegiaoRequest request) {
        Regiao regiao = regiaoRepository.findById(id)
                .orElseThrow(() -> new RegiaoNotFoundException(id));
        regiao.setNome(request.getNome());
        regiao.setLatitude(request.getLatitude());
        regiao.setLongitude(request.getLongitude());
        regiao.setDescricao(request.getDescricao());
        return toResponse(regiaoRepository.save(regiao));
    }

    public void deletar(Long id) {
        if (!regiaoRepository.existsById(id)) throw new RegiaoNotFoundException(id);
        regiaoRepository.deleteById(id);
    }

    private Usuario getUsuario(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    private RegiaoResponse toResponse(Regiao r) {
        return new RegiaoResponse(r.getId(), r.getNome(), r.getLatitude(), r.getLongitude(), r.getDescricao());
    }
}