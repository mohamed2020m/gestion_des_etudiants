<?php
    if ($_SERVER["REQUEST_METHOD"] == "GET") {
        include_once '../racine.php';
        include_once RACINE . '/service/EtudiantService.php';
        loadAll();
    }
    // function loadAll() {
    //     $es = new EtudiantService();
    //     header('Content-type: application/json');
        
    //     echo json_encode($es->findAllApi());
    // }

    function loadAll() {
        $es = new EtudiantService();
        $results = $es->findAllApi();

        // Iterate through the results and handle the 'photo' column
        foreach ($results as &$result) {
            // Check if the 'photo' column is not null
            if ($result['photo'] !== null) {
                // Convert the binary image data to Base64
                $result['photo'] = base64_encode($result['photo']);
            }
            // If the 'photo' column is null, you can leave it as is
        }
        header('Content-type: application/json');
        echo json_encode($results);
    }
    