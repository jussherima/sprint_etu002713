package controller;

import annotation.Controller;
import annotation.GetMapping;

@Controller
public class FrontController {

    @GetMapping(url = "/affiche bonjour")
    public void affiche_bonjour() {
        System.out.println("mande le controleur");
    }
}
