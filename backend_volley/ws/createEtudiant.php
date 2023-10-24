<?php
    
    if($_SERVER["REQUEST_METHOD"] == "POST"){
        include_once '../racine.php';
        include_once RACINE.'/service/EtudiantService.php';
        create();
    }

    function create(){
        extract($_POST);
        // if (isset($_FILES['photo'])) {
        //     $file = $_FILES['photo'];
    
        //     if ($file['error'] === UPLOAD_ERR_OK) {    
        //         $fileTmpName = $file['tmp_name'];
        //         $fileName = $file['name'];
        //         $photo = file_get_contents($fileTmpName);

        //         $es = new EtudiantService();
        //         $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe, $photo));
        
        //         //chargement de la liste des étudiants sous format json
        //         header('Content-type: application/json');
        //         echo json_encode($es->findAllApi());

        //         echo "File uploaded successfully.";
        //     } else {
        //         echo "File upload failed with error code: " . $file['error'];
        //     }
        // } else {
        //     echo "No file uploaded.";
        // }
        
        try{
            $es = new EtudiantService();
    
            $binaryImageData = base64_decode($photo);
    
            $es->create(new Etudiant(null, $nom, $prenom, $ville, $sexe, $binaryImageData));
    
            //chargement de la liste des étudiants sous format json
            header('Content-type: application/json');
            echo json_encode($es->findAllApi());
        } catch (PDOException $e) {
            echo "Database error: " . $e->getMessage();
        }
    }


    function createJson(){
        // Get the JSON data from the request body
        $json_data = file_get_contents("php://input");

        // Decode the JSON data
        $data = json_decode($json_data, true);

        if (isset($data['nom'])) {
            $nom =  $data['nom'];
            $prenom = $data['prenom'];
            $ville = $data['ville'];
            $sexe = $data['sexe'];
            $es = new EtudiantService();
            $es->create(new Etudiant(1, $nom, $prenom, $ville, $sexe));
            echo json_encode("Student created successfully.");

        } else {
            echo json_encode("Field 'nom' doesn't exist in the JSON data.");
        }
    }