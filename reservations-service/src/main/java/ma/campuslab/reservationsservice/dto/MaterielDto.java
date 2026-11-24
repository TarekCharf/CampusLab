package ma.campuslab.reservationsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MaterielDto {
    private String idMateriel;
    private String libelle;
    private int disponible;

    public MaterielDto materiels(MaterielDto materielDto) {
        this.idMateriel = materielDto.getIdMateriel();
        this.libelle = materielDto.getLibelle();
        this.disponible = materielDto.getDisponible();
        return this;
    }
}
