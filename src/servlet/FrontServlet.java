package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ModuleLayer.Controller;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.GET;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Mapping;

// @WebServlet(urlPatterns = "/*", name = "monservlet")
public class FrontServlet extends HttpServlet {
    List<String> liste_controller;
    HashMap<String,Mapping> mon_map;

    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        liste_controller = new ArrayList<>();
        mon_map =new HashMap<>();
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    
    @SuppressWarnings("unchecked")
    public void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // flux de sortie vers le navigateur
        PrintWriter out = response.getWriter();
        out.println("bienvenu dans le controller");
        out.println("voici votre url " + request.getRequestURI());

        // prendre l'url clicker
        String url = request.getRequestURI();
        // si l'url est juste /framework_test/ afficher la liste des controllers
        if (url.equals("/framework_test/")){
            // prendre le package qui contient les controller
            String context_Valeur  = getServletConfig().getInitParameter("controller");

            // prendre la liste des class dans le package 
            List<Class<?>> liste_class = getClasses(context_Valeur);

            // verifier si la class est annoter et l'ajouter dans la liste des controllers
            for(@SuppressWarnings("rawtypes") Class clazz : liste_class ){
                if(clazz.isAnnotationPresent(annotation.Controller.class)){
                    liste_controller.add(clazz.getName());
                    mon_map.putAll(getAnnoteMethods(clazz));
                }
            }
        }
        // prendre la fonction associe a l'url
        else{
            String context_Valeur  = getServletConfig().getInitParameter("controller");
            List<Class<?>> liste_class = getClasses(context_Valeur);
            for(@SuppressWarnings("rawtypes") Class clazz : liste_class ){
                if(clazz.isAnnotationPresent(annotation.Controller.class)){
                    liste_controller.add(clazz.getName());
                    mon_map.putAll(getAnnoteMethods(clazz));
                };
            }
        }
        
        // fonction qui affiche les fonction associer a l'url
        show_url_map(url, out);
    }   
    
    // afficher l'url
    public void show_url_map(String url,PrintWriter out){
        boolean url_existe = false;
        for(Map.Entry<String,Mapping> entry : this.mon_map.entrySet()){
            String valeur_url = "/framework_test/"+entry.getKey();
            if(valeur_url.equals(url)){
                url_existe = true;
                out.println("====================================================");
                out.println("nom du class est ="+entry.getValue().getClassName());
                out.println("nom du method ="+entry.getValue().getMethodName());
                out.println("====================================================");
            }
        }
        if(!url_existe){
            out.println("aucun method associer a cette url");
        }
    }
    
    // prendere toute les class dans le package specifier
    public List<Class<?>> getClasses(String packageName) {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        File directory = new File(Thread.currentThread().getContextClassLoader().getResource(packagePath).getFile());
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isFile() && file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    classes.add(clazz);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        return classes;
    }

    // prendre tou les mthod annoter dans cette class
    public HashMap<String,Mapping> getAnnoteMethods(Class<?> my_class){
        Method[] liste_method = my_class.getDeclaredMethods();
        HashMap<String,Mapping> liste_annoted_method = new HashMap<>();
        for(Method m : liste_method){
            if(m.isAnnotationPresent(GET.class)){
                String my_url = m.getAnnotation(GET.class).url();
                liste_annoted_method.put(my_url,new Mapping(my_class.getName(), m.getName()));
            }
        }
        return liste_annoted_method;
    }
    
}