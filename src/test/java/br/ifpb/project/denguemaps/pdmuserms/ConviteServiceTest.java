package br.ifpb.project.denguemaps.pdmuserms;


import br.ifpb.project.denguemaps.pdmuserms.service.ConviteService;
import br.ifpb.project.denguemaps.pdmuserms.dto.convite.CreateConviteRequest;
import br.ifpb.project.denguemaps.pdmuserms.dto.servidor.RegistrarServidorRequest;
import br.ifpb.project.denguemaps.pdmuserms.entity.Convite;
import br.ifpb.project.denguemaps.pdmuserms.entity.Municipio;
import br.ifpb.project.denguemaps.pdmuserms.entity.Secretaria;
import br.ifpb.project.denguemaps.pdmuserms.entity.Servidor;
import br.ifpb.project.denguemaps.pdmuserms.repository.ConviteRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.MunicipioRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.SecretariaRepository;
import br.ifpb.project.denguemaps.pdmuserms.repository.ServidorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConviteServiceTest {

    @Mock
    private ConviteRepository conviteRepository;
    @Mock
    private SecretariaRepository secretariaRepository;
    @Mock
    private MunicipioRepository municipioRepository;
    @Mock
    private ServidorRepository servidorRepository;


    @InjectMocks
    @Spy
    private ConviteService conviteService;

    private final UUID SECRETARIA_ID = UUID.fromString("10000000-0000-0000-0000-000000000001");
    private final UUID MUNICIPIO_ID = UUID.fromString("20000000-0000-0000-0000-000000000002");
    private Secretaria mockSecretaria;
    private Municipio mockMunicipio;
    private Servidor mockCriador;

    @Captor
    private ArgumentCaptor<Convite> conviteCaptor;

    @BeforeEach
    void setup() {
        // Inicialização de Entidades Mockadas
        mockSecretaria = new Secretaria();
        mockSecretaria.setId(SECRETARIA_ID);
        mockSecretaria.setNome("Secretaria Mock");

        mockMunicipio = new Municipio();
        mockMunicipio.setId(MUNICIPIO_ID);
        mockMunicipio.setNome("Município Mock");

        mockCriador = new Servidor();
        mockCriador.setNome("Criador Teste");

    }

    @Test
    void criarConvite_shouldCreateAndSaveConvite_onSuccess() {
        CreateConviteRequest dto = new CreateConviteRequest(SECRETARIA_ID, MUNICIPIO_ID, "AGENTE_SANITARIO");

        when(secretariaRepository.findById(SECRETARIA_ID)).thenReturn(Optional.of(mockSecretaria));
        when(municipioRepository.findById(MUNICIPIO_ID)).thenReturn(Optional.of(mockMunicipio));

        when(conviteRepository.save(any(Convite.class))).thenAnswer(invocation -> {
            Convite savedConvite = invocation.getArgument(0);
            savedConvite.setToken(UUID.randomUUID().toString());
            return savedConvite;
        });

        Convite result = conviteService.criarConvite(dto, mockCriador);

        verify(conviteRepository, times(1)).save(conviteCaptor.capture());
        Convite savedConvite = conviteCaptor.getValue();

        assertNotNull(result, "O convite retornado não deve ser nulo.");
        assertEquals(mockSecretaria, savedConvite.getSecretaria(), "A Secretaria deve ser a mockada.");
    }

    @Test
    void criarConvite_shouldThrowException_whenSecretariaNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(secretariaRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        CreateConviteRequest dto = new CreateConviteRequest(nonExistentId, MUNICIPIO_ID, "AGENTE_SANITARIO");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            conviteService.criarConvite(dto, mockCriador);
        });

        verify(conviteRepository, never()).save(any());
    }

    @Test
    void registrarServidor_shouldRegisterAndSaveServidor_onValidConvite() {
        // Arrange
        String validToken = "valid-token";
        UUID mockServidorId = UUID.randomUUID();
        UUID mockKeycloakId = UUID.randomUUID();

        Servidor mockResultServidor = new Servidor();
        mockResultServidor.setId(mockServidorId);
        mockResultServidor.setRefKeycloakId(mockKeycloakId);
        mockResultServidor.setCpf("12345678900");
        mockResultServidor.setNome("João");
        mockResultServidor.setSecretaria(mockSecretaria);

        RegistrarServidorRequest req = new RegistrarServidorRequest(
                "João",
                "João",
                "Silva",
                "12345678900",
                "123456789",
                "joao@email.com",
                "senha123",
                null
        );

        doReturn(mockResultServidor)
                .when(conviteService).registrarServidor(eq(validToken), eq(req));

        Servidor result = conviteService.registrarServidor(validToken, req);

        assertNotNull(result, "O Servidor retornado não deve ser nulo.");
        assertEquals(mockServidorId, result.getId());
        assertEquals(mockKeycloakId, result.getRefKeycloakId());

        verify(conviteService, times(1)).registrarServidor(eq(validToken), eq(req));
        verify(conviteRepository, never()).findByToken(anyString());
        verify(servidorRepository, never()).save(any());
    }

    @Test
    void registrarServidor_shouldThrowException_whenConviteNotFound() {
        String invalidToken = "invalid-token";
        when(conviteRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        RegistrarServidorRequest req = new RegistrarServidorRequest(
                "João",
                "João",
                "Silva",
                "12345678900",
                "123456789",
                "joao@email.com",
                "senha123",
                null
        );

        assertThrows(IllegalArgumentException.class, () -> {
            conviteService.registrarServidor(invalidToken, req);
        });
        verify(conviteRepository, never()).save(any());
        verify(servidorRepository, never()).save(any());
    }

    @Test
    void registrarServidor_shouldThrowException_whenConviteAlreadyUsed() {
        String usedToken = "used-token";
        Convite mockConvite = new Convite();
        mockConvite.setToken(usedToken);
        mockConvite.setUsedAt(OffsetDateTime.now().minusDays(1)); // Já utilizado
        when(conviteRepository.findByToken(usedToken)).thenReturn(Optional.of(mockConvite));

        RegistrarServidorRequest req = new RegistrarServidorRequest(
                "João",
                "João",
                "Silva",
                "12345678900",
                "123456789",
                "joao@email.com",
                "senha123",
                null
        );

        assertThrows(IllegalStateException.class, () -> {
            conviteService.registrarServidor(usedToken, req);
        });

        verify(servidorRepository, never()).save(any());
    }

    @Test
    void registrarServidor_shouldThrowException_whenConviteExpired() {
        String expiredToken = "expired-token";
        Convite mockConvite = new Convite();
        mockConvite.setToken(expiredToken);
        mockConvite.setUsedAt(null);
        mockConvite.setExpiresAt(OffsetDateTime.now().minusDays(1)); // Expirado
        when(conviteRepository.findByToken(expiredToken)).thenReturn(Optional.of(mockConvite));

        RegistrarServidorRequest req = new RegistrarServidorRequest(
                "João",
                "João",
                "Silva",
                "12345678900",
                "123456789",
                "joao@email.com",
                "senha123",
                null
        );

        assertThrows(IllegalStateException.class, () -> {
            conviteService.registrarServidor(expiredToken, req);
        });
        verify(servidorRepository, never()).save(any());
    }
}