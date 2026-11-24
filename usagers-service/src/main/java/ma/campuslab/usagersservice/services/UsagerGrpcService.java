package ma.campuslab.usagersservice.services;

import io.grpc.stub.StreamObserver;
import ma.campuslab.usagersservice.entities.EtatUsager;
import ma.campuslab.usagersservice.grpc.VerifierAdmissibiliteReponse;
import ma.campuslab.usagersservice.grpc.VerifierAdmissibiliteRequest;
import org.springframework.grpc.server.service.GrpcService;
import ma.campuslab.usagersservice.grpc.UsagerServiceGrpc;

/**
 * SERVEUR gRPC (service Usagers).
 * Étend la classe générée UsagerServiceImplBase, et implémente la méthode du .proto.
 */
@GrpcService  // Spring enregistre ce service automatiquement dans le serveur gRPC (port 9090)
public class UsagerGrpcService extends UsagerServiceGrpc.UsagerServiceImplBase {


    private final IUsagerService usagerService;

    public UsagerGrpcService(IUsagerService usagerService) {
        this.usagerService = usagerService;
    }

    @Override
    public void verifierAdmissibilite(VerifierAdmissibiliteRequest requete,
                                      StreamObserver<VerifierAdmissibiliteReponse> reponseObserver) {

        boolean admissible;
        String motif;

        var usager = usagerService.findUsagerById(requete.getIdUsager());
        if (usager == null) {
            admissible = false;
            motif = "USAGER_INCONNU";          // réponse métier, PAS une erreur gRPC
        } else if (usager.getEtat() == EtatUsager.SUSPENDU) {
            admissible = false;
            motif = "USAGER_SUSPENDU";
        } else {
            admissible = true;
            motif = "OK";
        }

        VerifierAdmissibiliteReponse reponse = VerifierAdmissibiliteReponse.newBuilder()
                .setAdmissible(admissible)
                .setMotif(motif)
                .build();

        reponseObserver.onNext(reponse);   // envoie la réponse
        reponseObserver.onCompleted();     // IMPORTANT : termine l'appel, sinon le client attend
    }
}