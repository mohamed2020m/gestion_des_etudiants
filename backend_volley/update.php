<!DOCTYPE html>
<?php
    include_once './racine.php';
    include_once RACINE.'/service/EtudiantService.php';

    extract($_GET);
    extract($_GET);
    $es = new EtudiantService();
    $et = $es->findById($id);

?>

<html>
    <head>
        <meta charset="UTF-8">
        <title></title>
    </head>

    <body>
        <form method="GET" action="controller/updateEtudiant.php">
            <input type="text" hidden name="id" value="<?php echo $et->getId() ?>"  />
            <fieldset>
                <legend>Modifier étudiant</legend>
                <table border="0">
                    <tr>
                        <td>Nom : </td>
                        <td><input type="text" name="nom" value="<?php echo $et->getNom() ?>" /></td>
                    </tr>
                    <tr>
                        <td>Prenom :</td>
                        <td><input type="text" name="prenom" value="<?php echo $et->getPrenom() ?>" /></td>
                    </tr>
                    <tr>
                        <td>Ville</td>
                        <td>
                            <select name="ville">
                                <option value="Marrakech" <?php if($et->getVille() == "Marrakech") echo "selected" ?>>Marrakech</option>
                                <option value="Rabat" <?php if($et->getVille() == "Rabat") echo "selected" ?>>Rabat</option>
                                <option value="Agadir" <?php if($et->getVille() == "Agadir") echo "selected" ?>>Agadir</option>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td>Sexe </td>
                        <td>
                            M<input type="radio" name="sexe" value="homme" <?php if ($et->getSexe() ==  "homme" ) echo "checked" ?> />
                            F<input type="radio" name="sexe" value="femme" <?php if ($et->getSexe() ==  "femme" ) echo "checked" ?> />
                        </td>
                    </tr>
                    <tr>
                        <td></td>
                        <td>
                            <input type="submit" value="Envoyer" />
                            <input type="reset" value="Effacer" />
                        </td>
                    </tr>
                </table>
            </fieldset>
        </form>
    </body>
</html>