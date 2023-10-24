<?php

    if ($_SERVER["REQUEST_METHOD"] == "POST") {
        include_once '../racine.php';
        include_once RACINE . '/service/EtudiantService.php';
        update();
    }

    function update() {
        extract($_POST);

        $es = new EtudiantService();
        $et = $es->findById($id);
        
        if(isset($nom)){
            $et->setNom($nom);
        }
        if(isset($prenom)){
            $et->setPrenom($prenom);
        }
        if(isset($ville)){
            $et->setVille($ville);
        }
        if(isset($sexe)){
            $et->setSexe($sexe);
        }
        
        if(isset($photo)){
            $binaryImageData = base64_decode($photo);
            $et->setPhoto($binaryImageData);
        }

        $es->update($et);
        
        header('Content-type: application/json');
        echo "updated!";
    }