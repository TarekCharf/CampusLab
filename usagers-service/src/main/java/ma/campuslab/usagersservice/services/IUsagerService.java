package ma.campuslab.usagersservice.services;

import ma.campuslab.usagersservice.entities.Usager;

public interface IUsagerService {

    public Usager findUsagerById(String idUsager);
    public String resetAllUsagers();
}
