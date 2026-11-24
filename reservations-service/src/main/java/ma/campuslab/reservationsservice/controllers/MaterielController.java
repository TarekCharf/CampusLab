package ma.campuslab.reservationsservice.controllers;

import ma.campuslab.reservationsservice.dto.MaterielDto;
import ma.campuslab.reservationsservice.services.IMaterielService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materiels")
public class MaterielController {


    private final IMaterielService materielService;

    public MaterielController(IMaterielService materielService) {
        this.materielService = materielService;
    }

    @GetMapping("/findAll")
    public ResponseEntity<List<MaterielDto>> findAllMateriel() {
        return ResponseEntity.status(HttpStatus.OK).body(materielService.listMateriel());
    }

    @DeleteMapping("/reset")
    public ResponseEntity<String> reset() {
        return ResponseEntity.status(HttpStatus.OK).body(materielService.resetMateriel());
    }

}
