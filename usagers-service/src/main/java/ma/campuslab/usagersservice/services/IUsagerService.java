package ma.campuslab.usagersservice.services;

import ma.campuslab.usagersservice.entities.Usager;

public interface IUsagerService {

    Usager findUsagerById(String idUsager);
    String resetAllUsagers();
}
