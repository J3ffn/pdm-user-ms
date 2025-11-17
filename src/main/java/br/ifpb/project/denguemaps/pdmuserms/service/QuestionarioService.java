package br.ifpb.project.denguemaps.pdmuserms.service;

import br.ifpb.project.denguemaps.pdmuserms.dto.cidadao.CidadaoResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.questionario.QuestionarioAtualizarDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.questionario.QuestionarioCriarDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.questionario.QuestionarioResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.dto.report.ReportResponseDTO;
import br.ifpb.project.denguemaps.pdmuserms.entity.Cidadao;
import br.ifpb.project.denguemaps.pdmuserms.entity.Questionario;
import br.ifpb.project.denguemaps.pdmuserms.entity.Report;
import br.ifpb.project.denguemaps.pdmuserms.repository.QuestionarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionarioService {
    private final QuestionarioRepository questionarioRepository;
    private final CidadaoService cidadaoService;

    public QuestionarioResponseDTO registrarQuestionarioComRetorno(
            QuestionarioCriarDTO questionarioCriarDTO,
            String token){
        Questionario questionario = new Questionario();
        aplicarCriacaoEntidadeQuestionarioSemRetorno(questionario, questionarioCriarDTO, token);
        questionario.setCreatedAt(OffsetDateTime.now());
        questionario.setUpdatedBy(OffsetDateTime.now());
        questionario = salvarQuestionario(questionario);
        return retornarResponseDTO(questionario);
    }

    public QuestionarioResponseDTO atualizarQuestionarioComRetorno(
            QuestionarioAtualizarDTO questionarioAtualizarDTO,
            String token){
        Questionario questionario = buscarQuestionarioPorId(questionarioAtualizarDTO.getId());
        aplicarMudancaEntidadeQuestionarioSemRetorno(questionario, questionarioAtualizarDTO, token);
        questionario.setUpdatedBy(OffsetDateTime.now());
        salvarQuestionario(questionario);
        return retornarResponseDTO(questionario);
    }

    public void deletarQuestionario(UUID idQuestionario){
        deletarQuestionarioEspecifico(idQuestionario);
    }

    public QuestionarioResponseDTO buscarQuestionarioEspecifico(UUID uuid){
        Questionario questionario = questionarioRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("Questionario não encontrado"));
        return retornarResponseDTO(questionario);
    }

    public List<QuestionarioResponseDTO> buscarQuestionarioCidadaoEspecifico(UUID idCidadao){
        List<Questionario> Listaquestionario = questionarioRepository.findAllByCidadaoId(idCidadao);
        return mapearReportsResponseDTO(Listaquestionario);
    }

    public List<QuestionarioResponseDTO> buscarTodoQuestionario(){
        List<Questionario> listaQuestionario = questionarioRepository.findAll();
        return mapearReportsResponseDTO(listaQuestionario);
    }



    // Metodos auxiliares:
    public void deletarTodoQuestionarioCidadaoEspecifico(UUID idCidadao){
        questionarioRepository.deleteAllByCidadaoId(idCidadao);
    }


    private Questionario salvarQuestionario(Questionario questionario){
        return questionarioRepository.save(questionario);
    }

    private void deletarQuestionarioEspecifico(UUID idQuestionario){
        questionarioRepository.deleteById(idQuestionario);
    }

    private Questionario buscarQuestionarioPorId(UUID idQuestionario){
        return questionarioRepository.findById(idQuestionario)
                .orElseThrow(() -> new IllegalArgumentException("Questionario não encontrado"));
    }

    private List<QuestionarioResponseDTO> mapearReportsResponseDTO(List<Questionario> questionario) {
        return questionario.stream()
                .map(this::retornarResponseDTO)
                .collect(Collectors.toList());
    }

    private QuestionarioResponseDTO retornarResponseDTO(Questionario questionario){
        QuestionarioResponseDTO questionarioResponseDTO = new QuestionarioResponseDTO();
        questionarioResponseDTO.setId(questionario.getId());
        questionarioResponseDTO.setPerguntas(questionario.getPerguntas());
        questionarioResponseDTO.setRespostas(questionario.getRespostas());
        questionarioResponseDTO.setCreatedAt(questionario.getCreatedAt());
        questionarioResponseDTO.setUpdatedBy(questionario.getUpdatedBy());
        questionarioResponseDTO.setCidadao(questionario.getCidadao());
        return questionarioResponseDTO;
    }
    private void aplicarMudancaEntidadeQuestionarioSemRetorno(
            Questionario questionario,
            QuestionarioAtualizarDTO questionarioAtualizarDTO,
            String token){
        questionario.setPerguntas(questionarioAtualizarDTO.getPerguntas());
        questionario.setRespostas(questionarioAtualizarDTO.getRespostas());
        questionario.setCidadao(buscarCidadao(questionarioAtualizarDTO.getFkCidadaoId(), token));
    }

    private void aplicarCriacaoEntidadeQuestionarioSemRetorno(
            Questionario questionario,
            QuestionarioCriarDTO questionarioCriarDTO,
            String token){
        questionario.setPerguntas(questionarioCriarDTO.getPerguntas());
        questionario.setRespostas(questionarioCriarDTO.getRespostas());
        questionario.setCidadao(buscarCidadao(questionarioCriarDTO.getFkCidadaoId(), token));
    }

    private Cidadao buscarCidadao(UUID idCidadao, String token){
        CidadaoResponseDTO cidadaoResponseDTO = cidadaoService.buscarCidadaoEspecificoId(idCidadao, token);
        Cidadao cidadao = new Cidadao();
        cidadao.setId(cidadaoResponseDTO.getId());
        // Adicionar mais setagem se precisar pegar outros dados de cidadao.
        return cidadao;
    }
}
