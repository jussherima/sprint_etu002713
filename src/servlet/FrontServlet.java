package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ModuleLayer.Controller;
import java.util.ArrayList;
import java.util.List;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// @WebServlet(urlPatterns = "/*", name = "monservlet")
public class FrontServlet extends HttpServlet {
    List<String> liste_controller;
    boolean est_checked;

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
        if (url.equals("/framework_test/")){ // si l'url est juste "/"
            String context_Valeur  = getServletConfig().getInitParameter("controller");
            out.println(context_Valeur);
            List<Class<?>> liste_class = getClasses(context_Valeur);
            for(@SuppressWarnings("rawtypes") Class clazz : liste_class ){
                clazz.isAnnotationPresent(Controller.class);
                out.println("voici un controller"+clazz.getName());
            }
        }
    }   

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

    
}