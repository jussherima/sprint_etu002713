package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import annotation.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = "/*", name = "monservlet")
public class MainServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @SuppressWarnings("unchecked")
    public void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
                response.getWriter().println("bienvenu dans le controller");
        response.getWriter().println("voici votre url " + request.getRequestURI());
        String url = request.getRequestURI();
        System.out.println("url = /framework_test/ ="+url);
        if (url.equals("/framework_test/")){
            List<Class<?>> liste_class = getClasses("controller");
            for(Class clazz : liste_class ){
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