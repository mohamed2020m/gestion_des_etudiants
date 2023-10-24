<?php
    include_once RACINE . '/classes/Etudiant.php';
    include_once RACINE . '/connexion/Connexion.php';
    include_once RACINE . '/dao/IDao.php';

    class EtudiantService implements IDao {
        private $connexion;
        function __construct() {
            $this->connexion = new Connexion();
        }
        
        public function create($o) {
            $query = "INSERT INTO Etudiant (`id`, `nom`, `prenom`, `ville`, `sexe`, `photo`) "
            . "VALUES (NULL, ?, ?, ?, ?, ?);";
            
            // Assign values to variables
            $nom = $o->getNom();
            $prenom = $o->getPrenom();
            $ville = $o->getVille();
            $sexe = $o->getSexe();
            $photo = $o->getPhoto();

            try {
                $stmt = $this->connexion->getConnexion()->prepare($query);
                // Bind variables to placeholders
                $stmt->bindParam(1, $nom);
                $stmt->bindParam(2, $prenom);
                $stmt->bindParam(3, $ville);
                $stmt->bindParam(4, $sexe);
                // $stmt->bindParam(5, $photo);
                $stmt->bindParam(5, $photo, PDO::PARAM_LOB);

                $stmt->execute();

                echo "Record inserted successfully.";
            } catch (PDOException $e) {
                echo "Database error: " . $e->getMessage();
            }
        }
        
        public function delete($o) {
            $query = "delete from Etudiant where id = " . $o->getId();
            $req = $this->connexion->getConnexion()->prepare($query);
            $req->execute() or die('Erreur SQL');
        }
        
        public function findAll() {
            $etds = array();
            $query = "select * from Etudiant";
            $req = $this->connexion->getConnexion()->prepare($query);
            $req->execute();
            while ($e = $req->fetch(PDO::FETCH_OBJ)) {
                $etds[] = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photo);
            }
            return $etds;
        }

        public function findById($id) {
            $query = "select * from Etudiant where id = " . $id;
            $req = $this->connexion->getConnexion()->prepare($query);
            $req->execute();
            if ($e = $req->fetch(PDO::FETCH_OBJ)) {
                $etd = new Etudiant($e->id, $e->nom, $e->prenom, $e->ville, $e->sexe, $e->photo);
            }
            return $etd;
        }

        public function update($o) {
            $query = "UPDATE `etudiant` SET `nom` = :nom, `prenom` = :prenom, `ville` = :ville, `sexe` = :sexe, `photo` = :photo WHERE `etudiant`.`id` = :id";

            // Assign values to variables
            $id = $o->getId();
            $nom = $o->getNom();
            $prenom = $o->getPrenom();
            $ville = $o->getVille();
            $sexe = $o->getSexe();
            $photo = $o->getPhoto();

            try {
                $stmt = $this->connexion->getConnexion()->prepare($query);
                $stmt->bindParam(':nom', $nom);
                $stmt->bindParam(':prenom', $prenom);
                $stmt->bindParam(':ville', $ville);
                $stmt->bindParam(':sexe', $sexe);
                $stmt->bindParam(':photo', $photo, PDO::PARAM_LOB);
                $stmt->bindParam(':id', $id);

                $stmt->execute();

                echo "Record updated successfully.";
            } catch (PDOException $e) {
                echo "Database error: " . $e->getMessage();
            }
        }

        public function findAllApi() {
            try {
                $query = "select * from Etudiant ORDER BY id DESC";
                $req = $this->connexion->getConnexion()->prepare($query);
                $req->execute();
                return $req->fetchAll(PDO::FETCH_ASSOC);
            } catch (PDOException $e) {
            
                echo "Database error: " . $e->getMessage();
                return null;
            }
        }
    }