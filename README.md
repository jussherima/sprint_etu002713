# Sprint 4:
  ->fonctionnalite
    -> creer une class ModelView    
        ->HashMap<nom_objet,objet> pour stocker les donner envoyer vers le VIEW
        ->String nom view
    ->du cote du front servlet :
        ->si la fonction de l'url retourne une ModelView on affiche le view 
          ->on fait une dispatch vers le view et on envoyons les donners en meme temps
        ->si l'url retourne une string on affiche la chaine de caractere retourner