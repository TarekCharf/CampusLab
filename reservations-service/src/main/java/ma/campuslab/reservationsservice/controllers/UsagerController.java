package ma.campuslab.reservationsservice.controllers;

import ma.campuslab.reservationsservice.grpc.UsagerGrpcClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usagers")
public class UsagerController {

    private final UsagerGrpcClient usagerGrpcClient;

    public UsagerController(UsagerGrpcClient usagerGrpcClient) {
        this.usagerGrpcClient = usagerGrpcClient;
    }

    @GetMapping("/{idUsager}")
    public ResponseEntity<UsagerGrpcClient.Admissibilite> getUsagerById(@PathVariable("idUsager") String idUsager){
        return ResponseEntity.status(HttpStatus.OK).body(usagerGrpcClient.verifier(idUsager));
    }

}
