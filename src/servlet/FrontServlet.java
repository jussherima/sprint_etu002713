package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import annotation.GET;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Mapping;
import util.ModelView;

// @WebServlet(urlPatterns = "/*", name = "monservlet")
public class FrontServlet extends HttpServlet {
    List<String> liste_controller;
    HashMap<String,Mapping> mon_map;

    // intialization du servlet
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException{
        super.init(config);
        liste_controller = new ArrayList<>();
        mon_map =new HashMap<>();

        // maka ny fonction si class annoter rehetra
        String context_Valeur  = getServletConfig().getInitParameter("controller");
        List<Class<?>> liste_class = getClasses(context_Valeur);
        for(@SuppressWarnings("rawtypes") Class clazz : liste_class ){
            if(clazz.isAnnotationPresent(annotation.Controller.class)){
                liste_controller.add(clazz.getName());
                mon_map.putAll(getAnnoteMethods(clazz));
            };
        }        

    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // flux de sortie vers le navigateur
        PrintWriter out = response.getWriter();

        // prendre l'url clicker
        
        String path = request.getRequestURI().split("/")[2];
        try{
            if(mon_map.containsKey(path)){
                invoke_method(path,request,response);
            }else{
                out.println("voici le path "+path);
                // RequestDispatcher req = request.getRequestDispatcher("/view");
                for(Map.Entry<String, Mapping> entry  : mon_map.entrySet()){
                    out.println(path+"ve == amin'ny "+entry.getKey());
                }
            }
        }catch(Exception e){
            out.println("misy erreur le izy:"+e.getMessage());
        }
    }   




    // afficher l'url
    private void show_url_map(String url,HttpServletRequest request,HttpServletResponse response) throws IOException{
        boolean url_existe = false;
        PrintWriter out = response.getWriter();
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
    
    // proceder l'url lannotation
    private void invoke_method(String url,HttpServletRequest req,HttpServletResponse res) throws Exception{
        boolean url_existe = false;

        for(Map.Entry<String,Mapping> entry : this.mon_map.entrySet()){
            String valeur_url = entry.getKey();
            res.getWriter().println(valeur_url+" ve egale a "+url);
            if(valeur_url.equals(url)){
                url_existe = true;
                // prendre la class avec son nom
                Class<?> clazz = Class.forName(entry.getValue().getClassName());

                Method m = clazz.getDeclaredMethod(entry.getValue().getMethodName(),null);
                
                Object retour = m.invoke(clazz.newInstance(),null);
                
                // dans le cas ou le retour de la method est une string
                res.getWriter().println("mon type de donner est"+retour.getClass());
                if(retour.getClass() == String.class){
                    res.getWriter().println((String)retour);
                }else if(retour.getClass() == ModelView.class){
                    res.getWriter().println("l'instance de la class est une view");
                    trait_view(req, res, (ModelView)retour);
                }
                break;
            }
        }
        if(!url_existe){
            res.getWriter().println("aucun method associer a cette url");
        }
    }

    // traitement du modelAndView
    private void trait_view(HttpServletRequest request,HttpServletResponse response,ModelView view) throws ServletException, IOException{
        // creer une dispatcher de servelt
        RequestDispatcher dispatcher = request.getRequestDispatcher("/"+view.getName());

        // affecter les data dans le ModelView vers le dispatcher
        for(Map.Entry<String,Object> entry : view.getData().entrySet()){
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        // dispatcher vers le view
        dispatcher.forward(request, response);
    }

    // prendere toute les class dans le package specifier
    private List<Class<?>> getClasses(String packageName) {
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
    private HashMap<String,Mapping> getAnnoteMethods(Class<?> my_class){
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