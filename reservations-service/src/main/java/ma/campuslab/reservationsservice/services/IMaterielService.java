package ma.campuslab.reservationsservice.services;

import ma.campuslab.reservationsservice.dto.MaterielDto;
import ma.campuslab.reservationsservice.messaging.MessageEnvelope;

import java.util.List;

public interface IMaterielService {
    public MessageEnvelope publishMessage(String idMateriel);
    public List<MaterielDto> listMateriel();
    public String resetMateriel();
    public List<MaterielDto> listResponse(MessageEnvelope message);
    public String resetResponse(MessageEnvelope message);

}
