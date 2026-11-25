package ma.campuslab.inventaireservice.controllers;

import ma.campuslab.inventaireservice.entities.Materiel;
import ma.campuslab.inventaireservice.services.IMaterielService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/materiels")
public class MaterielController {

    private final IMaterielService materielService;

    public MaterielController(IMaterielService materielService) {
        this.materielService = materielService;
    }

    @GetMapping("{idMateriel}")
    public ResponseEntity<Materiel> getMaterielById(@PathVariable String idMateriel) {
        return ResponseEntity.status(HttpStatus.OK).body(materielService.findMaterielById(idMateriel).orElseThrow());
    }
}
