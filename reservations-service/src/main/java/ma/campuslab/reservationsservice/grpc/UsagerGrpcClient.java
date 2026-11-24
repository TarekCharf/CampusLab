package ma.campuslab.reservationsservice.grpc;

import io.grpc.StatusRuntimeException;
import ma.campuslab.usagersservice.grpc.UsagerServiceGrpc;
import ma.campuslab.usagersservice.grpc.VerifierAdmissibiliteReponse;
import ma.campuslab.usagersservice.grpc.VerifierAdmissibiliteRequest;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UsagerGrpcClient {

    private final UsagerServiceGrpc.UsagerServiceBlockingStub usagerStub;

    public UsagerGrpcClient(UsagerServiceGrpc.UsagerServiceBlockingStub usagerStub) {
        this.usagerStub = usagerStub;
    }

    public record Admissibilite(boolean admissible, String motif) {}

    public Admissibilite verifier(String idUsager) {
        try {
            VerifierAdmissibiliteReponse reponse = usagerStub
                    .withDeadlineAfter(2, TimeUnit.SECONDS)   // échéance de 2 s, à poser à CHAQUE appel
                    .verifierAdmissibilite(
                            VerifierAdmissibiliteRequest.newBuilder()
                                    .setIdUsager(idUsager)
                                    .build());
            return new Admissibilite(reponse.getAdmissible(), reponse.getMotif());

        } catch (StatusRuntimeException e) {
            // UNAVAILABLE (service arrêté) ou DEADLINE_EXCEEDED (trop lent) => erreur technique => HTTP 503
            throw new UsagersIndisponibleException(
                    "Service Usagers indisponible (" + e.getStatus().getCode() + ")", e);
        }
    }

    public static class UsagersIndisponibleException extends RuntimeException {
        public UsagersIndisponibleException(String message, Throwable cause) { super(message, cause); }
    }
}