# CampusLab – Réservation de matériel pédagogique

Projet individuel du module **Architectures logicielles** (Pr O. ZAHOUR, A.U. 2026-2027).
**Auteur :** Charf Mohamed Tarek – **Version du document :** [25/11/2026]

Application distribuée composée de trois services Spring Boot (REST, gRPC, RabbitMQ) et d'une base MySQL, exécutés avec Docker Compose.

| Service | Rôle | Données de référence |
|---|---|---|
| `reservations-service` | API REST, cycle de vie des réservations, vue de suivi (CQRS) | demandes, statuts, motifs, versions d'état |
| `inventaire-service` | disponible du matériel, allocations, décisions | matériels, disponible, allocations, décisions |
| `usagers-service` | admissibilité d'un usager (serveur gRPC) | usagers fictifs |
| RabbitMQ | transport des commandes et des événements | – |

---

## 1. Prérequis et versions

| Outil | Version utilisée | Remarque |
|---|---|---|
| Docker Desktop (Compose v2) | 29.9.2 | seul prérequis obligatoire pour lancer le projet |
| Java (JDK) | 21 (Temurin) | uniquement dans les images Docker, pas nécessaire sur la machine |
| Maven | 3.9 | idem, utilisé dans l'image de build |
| Spring Boot | 4.1.3 | Spring Framework 7.0.11, Hibernate 7.4 |
| gRPC (grpc-java) | 1.84.1 | contrat : `src/main/protobuf/usagers.proto` |
| MySQL | 9.6 | image `mysql:9.6` |
| RabbitMQ | 3.13 (avec interface de gestion) | image `rabbitmq:3.13-management` |

Aucune installation de Java, Maven, MySQL ou RabbitMQ n'est nécessaire : tout est construit et lancé dans des conteneurs. **Prévoir plusieurs minutes pour le premier lancement** (téléchargement des images et des dépendances Maven).

## 2. Contenu de l'archive

```
CampusLab/
├── docker-compose.yaml          # MySQL, RabbitMQ et les 3 services
├── db/init/01-init.sql          # crée les bases et les utilisateurs MySQL (1er démarrage)
├── reservations-service/        # Dockerfile, pom.xml, src/ (dont le .proto)
├── inventaire-service/
├── usagers-service/             # serveur gRPC, contient aussi le .proto
└── README.md
```

## 3. Ports et adresses locales

| Élément | Adresse locale | Usage |
|---|---|---|
| Réservations (REST) | http://localhost:8081 | API du client de démonstration |
| Inventaire (REST) | http://localhost:8082 | `GET /materiels/{id}` (lecture pour les essais) |
| Usagers (HTTP) | http://localhost:8083 | Actuator |
| Usagers (gRPC) | `localhost:9091` | test avec Postman ou grpcurl (TLS désactivé) |
| RabbitMQ (AMQP) | `localhost:5672` | utilisé par les services |
| RabbitMQ (interface) | http://localhost:15672 | suivi des files, publication de messages |
| MySQL | `localhost:3306` | accès direct pour vérification |

Entre conteneurs, les services se joignent par leur nom : `mysql:3306`, `rabbitmq:5672`, `usagers-service:9091`.

### Paramètres d'exemple (environnement local uniquement)

| Élément | Identifiant | Mot de passe |
|---|---|---|
| MySQL (administrateur) | `root` | `root` |
| Base `reservations_db` / `suivi_db` | `reservations_user` | `reservations_pass` |
| Base `inventaire_db` | `inventaire_user` | `inventaire_pass` |
| Base `usagers_db` | `usagers_user` | `usagers_pass` |
| RabbitMQ | `campuslab` | `campuslab123` |

Ces valeurs sont des exemples pour un poste de développement. Aucun secret personnel n'est inclus dans l'archive. Elles sont définies dans `docker-compose.yaml` et `db/init/01-init.sql`.

## 4. Démarrage

### 4.1 Lancement (une commande)

Depuis le dossier qui contient `docker-compose.yaml` :

```bash
docker compose -p campuslab up -d --build
```

L'option `-p campuslab` fixe le nom du projet Compose : les conteneurs s'appellent alors toujours `campuslab-mysql`, `campuslab-rabbitmq`, `campuslab-reservations-service-1`, etc., quel que soit le nom du dossier de l'archive.

Compose respecte l'ordre suivant, grâce aux `depends_on` et aux contrôles de santé :

1. **MySQL** et **RabbitMQ** démarrent d'abord ; le lancement attend qu'ils soient `healthy`.
2. **Usagers** démarre (serveur gRPC sur 9091).
3. **Inventaire** démarre et se met à l'écoute de la file `inventaire.commandes`.
4. **Réservations** démarre en dernier (client gRPC vers Usagers, consommateur des résultats et de la vue de suivi).

### 4.2 Vérifier que tout est prêt

```bash
docker compose -p campuslab ps
```

Les cinq conteneurs doivent être `Up` (MySQL et RabbitMQ `healthy`). Contrôles rapides :

```bash
curl http://localhost:8082/materiels/M01        # {"idMateriel":"M01","libelle":"Vidéoprojecteur","disponible":3}
curl http://localhost:8081/suivi/reservations/findAll   # [] au départ
```

Dans l'interface RabbitMQ (http://localhost:15672, onglet *Queues*), les files durables `inventaire.commandes`, `reservations.resultats` et `suivi.etat` doivent exister.

### 4.3 Données de départ

Le jeu de départ est chargé automatiquement au démarrage des services (les tables sont créées par Hibernate, `ddl-auto=update`) :

| Usager | État | | Matériel | Libellé | Disponible |
|---|---|---|---|---|---|
| U01 | Actif | | M01 | Vidéoprojecteur | 3 |
| U02 | Suspendu | | M02 | Microphone | 1 |
| U03 | Actif | | M03 | Caméra | 0 |

`U99` n'existe pas (usager inconnu). Aucune réservation ni allocation au départ.

### 4.4 Arrêt

```bash
docker compose -p campuslab down        # arrête et supprime les conteneurs, conserve les données
docker compose -p campuslab down -v     # supprime aussi les volumes (base et files RabbitMQ)
```

## 5. Procédure de réinitialisation

### 5.1 Retour rapide au jeu de départ (entre deux tests)

Sans redémarrer les services : vide les réservations, la vue de suivi, les décisions et allocations, et remet le disponible à 3 / 1 / 0.

```bash
curl http://localhost:8081/reservations/deleteAll       # Supprime les données des réservation et la vue de suivi
curl http://localhost:8081/materiels/reset              # Réinitialise les données de l'inventaire (jeu de données de départ)
```

### 5.2 Réinitialisation complète (environnement vierge)

```bash
docker compose -p campuslab down -v
docker compose -p campuslab up -d --build
```

Tout est recréé : bases, utilisateurs, tables, données de départ, files RabbitMQ.

## 6. Exemple de parcours exécutable

Exemples en **PowerShell** (`curl.exe`). Les mêmes appels fonctionnent en Bash avec des quotes simples autour du JSON.

**1. Disponible initial de M01 (attendu : 3)**
```powershell
curl.exe -s http://localhost:8082/materiels/M01
```

**2. Créer une réservation : U01 demande 2 unités de M01 (attendu : 201, `EN_ATTENTE`, `versionEtat` 1)**
```powershell
$rep = curl.exe -s -i -X POST http://localhost:8081/reservations/add -H "Content-Type: application/json" -d "{\"idUsager\":\"U01\",\"idMateriel\":\"M01\",\"quantite\":2}"
$rep
$id = ($rep | Select-Object -Last 1 | ConvertFrom-Json).idReservation
```

**3. Consulter le résultat (attendu après quelques instants : `CONFIRMEE`, version 2) puis le disponible (attendu : 1)**
```powershell
curl.exe -s http://localhost:8081/find/{idReservation}
curl.exe -s http://localhost:8082/materiels/M01
```

**4. Vue de suivi (attendu : une seule ligne pour la réservation)**
```powershell
curl.exe -s http://localhost:8081/suivi/reservations/findAll
curl.exe -s http://localhost:8081/suivi/reservations/find/{idsuiviReservation}
```

**5. Annuler la réservation confirmée (attendu : 202, `ANNULATION_EN_COURS`, puis `ANNULEE`, M01 revient à 3)**
```powershell
curl.exe -s -i -X POST http://localhost:8081/reservations/$id/annulations
Start-Sleep -Seconds 2
curl.exe -s http://localhost:8081/reservations/$id
curl.exe -s http://localhost:8082/materiels/M01
```

**6. Cas d'erreur**
```powershell
# usager suspendu -> 422, motif USAGER_SUSPENDU
curl.exe -s -i -X POST http://localhost:8081/reservations/add -H "Content-Type: application/json" -d "{\"idUsager\":\"U02\",\"idMateriel\":\"M01\",\"quantite\":1}"
# usager inconnu -> 422, motif USAGER_INCONNU
curl.exe -s -i -X POST http://localhost:8081/reservations/add -H "Content-Type: application/json" -d "{\"idUsager\":\"U99\",\"idMateriel\":\"M01\",\"quantite\":1}"
# quantité nulle -> 400
curl.exe -s -i -X POST http://localhost:8081/reservations/add -H "Content-Type: application/json" -d "{\"idUsager\":\"U01\",\"idMateriel\":\"M01\",\"quantite\":0}"
# stock insuffisant : U01 demande M03 (disponible 0) -> 201 puis statut REFUSEE (STOCK_INSUFFISANT)
curl.exe -s -i -X POST http://localhost:8081/reservations/add -H "Content-Type: application/json" -d "{\"idUsager\":\"U01\",\"idMateriel\":\"M03\",\"quantite\":1}"
```

**7. Appel gRPC direct vers Usagers** (réflexion activée, sans fichier `.proto`) :
```bash
grpcurl -plaintext -d '{"idUsager":"U01"}' localhost:9091 campuslab.usagers.v1.UsagerService/VerifierAdmissibilite
```
Attendu : `{"admissible": true, "motif": "OK"}`.

## 7. Dépannage

| Symptôme | Cause probable et remède |
|---|---|
| `port is already allocated` sur 5672, 15672 ou 3306 | un autre conteneur ou service local utilise ce port : l'arrêter (`docker ps`) ou changer le port publié dans `docker-compose.yaml` |
| Un service redémarre en boucle | consulter `docker compose -p campuslab logs <service>` : base non prête, mauvais identifiants, table en conflit |
| Erreur `Access denied` à la connexion MySQL | le script `db/init` ne s'exécute qu'avec un volume vide : refaire `down -v` puis `up -d --build` |
| Réservation bloquée à `EN_ATTENTE` | Inventaire arrêté ou en erreur : voir ses logs ; les messages restent dans `inventaire.commandes` et sont traités au redémarrage |
| `503` à la création | le service Usagers est arrêté ou ne répond pas dans les 2 secondes |
| Modification du code sans effet | reconstruire l'image : `docker compose -p campuslab up -d --build <service>` |

Consulter les journaux d'un service, corrélés par `idReservation` :

```bash
docker compose -p campuslab logs reservations-service | grep RES-001
```
(PowerShell : `docker compose -p campuslab logs reservations-service | Select-String RES-001`)
