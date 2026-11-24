package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.dto.MaterielDto;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;

import java.util.List;

public interface IMaterielService {
    List<MaterielDto> listMateriel();
    String resetMateriel();
    List<MaterielDto> listResponse(MessageEnvelope message);
    String resetResponse(MessageEnvelope message);

}
