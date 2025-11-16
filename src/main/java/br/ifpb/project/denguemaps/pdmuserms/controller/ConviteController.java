package br.ifpb.project.denguemaps.pdmuserms.controller;

import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.RegistrarServidorRequest;
import br.ifpb.project.denguemaps.pdmuserms.entity.Convite;
import br.ifpb.project.denguemaps.pdmuserms.entity.Servidor;
import br.ifpb.project.denguemaps.pdmuserms.dto.convite.CreateConviteRequest;
import br.ifpb.project.denguemaps.pdmuserms.dto.convite.CreateConviteResponse;
import br.ifpb.project.denguemaps.pdmuserms.repository.ServidorRepository;
import br.ifpb.project.denguemaps.pdmuserms.service.ConviteService;
import br.ifpb.project.denguemaps.pdmuserms.service.ServidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ConviteController {

    private final ConviteService conviteService;
    private final ServidorService servidorService;
    private final ServidorRepository servidorRepository;

    @Value("${app.base-url:http://localhost:5173}")
    private String baseUrl;

    @PostMapping("/convites")
    public ResponseEntity<CreateConviteResponse> criarConvite(
            @RequestBody CreateConviteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String sub = jwt.getSubject();

        Servidor servidor = servidorService.getFindByKeycloakID(sub);

        Convite convite = conviteService.criarConvite(request, servidor);

        return ResponseEntity.ok(new CreateConviteResponse(
                baseUrl + "/auth/cadastro/" + convite.getToken(),
                convite.getExpiresAt().toString()
        ));
    }

    @PostMapping("/servidores/registro")
    public String registrarServidor(
            @RequestParam("token") String token,
            @RequestBody RegistrarServidorRequest request
    ) {
        Servidor servidor = conviteService.registrarServidor(token, request);
        return "Servidor criado: " + servidor.getNome();
    }
}