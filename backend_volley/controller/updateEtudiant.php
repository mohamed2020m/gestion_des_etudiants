<?php
    include_once '../racine.php';
    include_once RACINE.'/service/EtudiantService.php';

    extract($_GET);
    $es = new EtudiantService();
    $et = $es->findById($id);
    
    $et->setNom($nom);
    $et->setPrenom($prenom);
    $et->setVille($ville);
    $et->setSexe($sexe);

    $es->update($et);
    header("location:../index.php");
