package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotation.GET;
import annotation.RequestBody;
import exception.ControllerFolderNotFoundException;
import exception.DuplicateUrlException;
import exception.InvalideFunctionRetourException;
import exception.NoSuchUrlExcpetion;
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
    HashMap<String, Mapping> mon_map;
    Exception error = null;

    // intialization du servlet
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        liste_controller = new ArrayList<>();
        mon_map = new HashMap<>();

        // maka ny fonction si class annoter rehetra
        String context_Valeur = getServletConfig().getInitParameter("controller");
        List<Class<?>> liste_class = new ArrayList<>();
        try {
            liste_class = getClasses(context_Valeur);
        } catch (ControllerFolderNotFoundException ex) {
            error = ex;
        }

        for (@SuppressWarnings("rawtypes")
        Class clazz : liste_class) {
            if (clazz.isAnnotationPresent(annotation.Controller.class)) {
                liste_controller.add(clazz.getName());
                try {
                    mon_map.putAll(getAnnoteMethods(clazz));
                } catch (DuplicateUrlException exc) {
                    error = exc;
                }
            }
        }

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    // fonction qui traite toutes les requettes du client
    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // flux de sortie vers le navigateur
        PrintWriter out = response.getWriter();

        // prendre l'url clicker
        if (error == null) {
            String path = request.getRequestURI().split("/")[2];
            try {
                invoke_method(path, request, response);
            } catch (NoSuchUrlExcpetion e) {
                erreur(out, 2, e);
            } catch (InvalideFunctionRetourException e) {
                erreur(out, 3, e);
            } catch (Exception e) {
                out.println("misy erreur le izy:" + e.getMessage());
            }
        } else {
            if (error instanceof ControllerFolderNotFoundException) {
                erreur(out, 2);
            } else if (error instanceof DuplicateUrlException) {
                erreur(out, 1);
            }
        }
    }

    // afficher l'url
    private void show_url_map(String url, HttpServletRequest request, HttpServletResponse response) throws IOException {
        boolean url_existe = false;
        PrintWriter out = response.getWriter();
        for (Map.Entry<String, Mapping> entry : this.mon_map.entrySet()) {
            String valeur_url = "/framework_test/" + entry.getKey();
            if (valeur_url.equals(url)) {
                url_existe = true;
                out.println("====================================================");
                out.println("nom du class est =" + entry.getValue().getClassName());
                out.println("nom du method =" + entry.getValue().getMethodName());
                out.println("====================================================");
            }
        }
        if (!url_existe) {
            out.println("aucun method associer a cette url");
        }
    }

    // proceder l'url lannotation
    @SuppressWarnings("unused")
    private void invoke_method(String url, HttpServletRequest req, HttpServletResponse res)
            throws NoSuchUrlExcpetion, InvalideFunctionRetourException, Exception {
        boolean url_existe = false;

        for (Map.Entry<String, Mapping> entry : this.mon_map.entrySet()) {
            String valeur_url = entry.getKey();
            if (valeur_url.equals(url)) {
                url_existe = true;
                // prendre la class avec son nom
                Class<?> clazz = Class.forName(entry.getValue().getClassName());

                Method m = clazz.getDeclaredMethod(entry.getValue().getMethodName(), entry.getValue().method_param());

                @SuppressWarnings("deprecation")
                Object retour = m.invoke(clazz.newInstance(),
                        get_request_param(req, res, entry.getValue().liste_param()));

                // dans le cas ou le retour de la method est une string
                if (retour.getClass() == String.class) {
                    res.getWriter().println((String) retour);
                } else if (retour.getClass() == ModelView.class) {
                    res.getWriter().println("l'instance de la class est une view");
                    trait_view(req, res, (ModelView) retour);
                } else {
                    throw new InvalideFunctionRetourException("retour du fonction invalide");
                }
                break;
            }
        }
        if (!url_existe) {
            // res.getWriter().println("aucun method associer a cette url");
            throw new NoSuchUrlExcpetion("l'url que vous avez saisie n'existe pas");

        }
    }

    // traitement du modelAndView
    private void trait_view(HttpServletRequest request, HttpServletResponse response, ModelView view)
            throws ServletException, IOException {
        // creer une dispatcher de servelt
        RequestDispatcher dispatcher = request.getRequestDispatcher("/" + view.getName());

        // affecter les data dans le ModelView vers le dispatcher
        for (Map.Entry<String, Object> entry : view.getData().entrySet()) {
            request.setAttribute(entry.getKey(), entry.getValue());
        }

        // dispatcher vers le view
        dispatcher.forward(request, response);
    }

    // prendere toute les class dans le package specifier
    private List<Class<?>> getClasses(String packageName) throws ControllerFolderNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        String packagePath = packageName.replace('.', '/');
        File directory = null;
        try {
            directory = new File(Thread.currentThread().getContextClassLoader().getResource(packagePath).getFile());

        } catch (Exception e) {
            // TODO: handle exception
            throw new ControllerFolderNotFoundException("le controlleur que vous avez specifier n'existe pas");
        }

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
    private HashMap<String, Mapping> getAnnoteMethods(Class<?> my_class) throws DuplicateUrlException {
        Method[] liste_method = my_class.getDeclaredMethods();
        HashMap<String, Mapping> liste_annoted_method = new HashMap<>();
        for (Method m : liste_method) {
            if (m.isAnnotationPresent(GET.class)) {
                String my_url = m.getAnnotation(GET.class).url();
                if (liste_annoted_method.containsKey(my_url)) {
                    throw new DuplicateUrlException("l'url " + my_url + " contient 2 fonction en meme temps ");
                }
                // prendre les nom des parametre avec leurs valeur type
                HashMap<String, Class<?>> method_liste = new HashMap<>();
                for (Parameter param : m.getParameters()) {
                    try {
                        method_liste.put(param.getAnnotation(RequestBody.class).name(), String.class);
                    } catch (Exception e) {
                        method_liste.put("name", String.class);
                        // TODO: handle exception
                    }
                }
                liste_annoted_method.put(my_url, new Mapping(my_class.getName(), m.getName(), method_liste));
            }
        }
        return liste_annoted_method;
    }

    // fonction pour prendre tous les valeurs dans la requette
    public Object[] get_request_param(HttpServletRequest request, HttpServletResponse response,
            List<String> liste_objet)
            throws IOException {
        Object[] objet = new Object[liste_objet.size()];
        for (int i = 0; i < liste_objet.size(); i++) {
            objet[i] = request.getParameter(liste_objet.get(i));
        }
        return objet;
    }

    /*
     * 
     * fonction qui affiche les erreurs
     * 
     */
    private void erreur(PrintWriter out, int numero) {
        out.println("<h2>erreur " + numero + "</h1>");
        out.println("<a style=\"color:red\"  >" + error.getMessage() + "</a>");
        out.println("<a style=\"color:red\"  >");
        error.printStackTrace(out);
    }

    private void erreur(PrintWriter out, int numero, Exception error) {
        out.println("<h2>erreur " + numero + "</h1>");
        out.println("<a style=\"color:red\"  >" + error.getMessage() + "</a>");
        out.println("<a style=\"color:red\"  >");
        error.printStackTrace(out);
    }

}