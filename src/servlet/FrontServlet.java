package servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.InvalidParameterException;
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
import jakarta.servlet.http.HttpSession;
import util.Mapping;
import util.ModelView;
import util.Session;

// @WebServlet(urlPatterns = "/*", name = "monservlet")
public class FrontServlet extends HttpServlet {
    List<String> liste_controller;
    HashMap<String, Mapping> mon_map;
    Exception error = null;
    List<String> liste_nom = new ArrayList<>();

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
                } catch (Exception e) {

                    error = e;
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
                e.printStackTrace(out);
            }
        } else {
            if (error instanceof ControllerFolderNotFoundException) {
                erreur(out, 2);
            } else if (error instanceof DuplicateUrlException) {
                erreur(out, 1);
            } else {
                out.println(error.getMessage());
            }
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
                // verfie si les parametres sont tous annoters
                if (entry.getValue().getArgument().containsKey("error")) {
                    throw new Exception("ETU 002713 ereur pas de annotation detecter");
                }

                url_existe = true;
                // prendre la class avec son nom
                Class<?> clazz = Class.forName(entry.getValue().getClassName());
                Method m = null;
                try {
                    m = clazz.getDeclaredMethod(entry.getValue().getMethodName(),
                            entry.getValue().method_param());
                } catch (Exception e) {
                    throw new Exception(
                            "exception trouver sur la method invoke_method sur le get DeclaredMethod"
                                    + "<br/>le nom de la class est " + clazz.getName()
                                    + "<br/>le nom de la method est " + entry.getValue().getMethodName()
                                    + "<br/>la longeur du parametre est " + entry.getValue().method_param().length
                                    + entry.getValue().method_param()[0].getName());
                }

                @SuppressWarnings("deprecation")

                // minstancer an'le session ao anaty objet
                Object objet = clazz.newInstance();
                initialize_session(objet, req);
                Object retour = m.invoke(objet,
                        get_request_param(req, res, entry.getValue().getArgument()));

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

    private void trait_session(HttpSession session) {

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
            throw new ControllerFolderNotFoundException(
                    "le controlleur que vous avez specifier n'existe pas " + packageName);
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
    private HashMap<String, Mapping> getAnnoteMethods(Class<?> my_class) throws DuplicateUrlException, Exception {
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

                Class<?>[] params = m.getParameterTypes();

                for (Parameter param : m.getParameters()) {

                    try {
                        method_liste.put(param.getAnnotation(RequestBody.class).name(),
                                param.getType());
                    } catch (Exception e) {
                        method_liste.put("error", String.class);
                        // throw new Exception("misy parametre tsy annoter");
                    }
                }
                liste_annoted_method.put(my_url, new Mapping(my_class.getName(), m.getName(), method_liste));
            }
        }
        return liste_annoted_method;
    }

    // fonction pour prendre tous les valeurs dans la requette
    public Object[] get_request_param(HttpServletRequest request, HttpServletResponse response,
            HashMap<String, Class<?>> liste_objet)
            throws IOException, NumberFormatException, IllegalArgumentException, IllegalAccessException, Exception {
        Object[] objet = new Object[liste_objet.size()];
        int i = 0;
        for (Map.Entry<String, Class<?>> entry : liste_objet.entrySet()) {
            // initialiser les valeurs string

            if (entry.getValue() == String.class) {
                try {
                    objet[i] = request.getParameter(entry.getKey());
                } catch (Exception e) {
                    objet[i] = "null";
                }
            }
            // initialiser les objet
            else {
                Class<?> objet_param = entry.getValue();
                try {
                    objet[i] = initialize_from_param(request, response, objet_param);
                } catch (Exception e) {
                    response.getWriter().println("misy erreur le initialize from param");
                    throw e;
                }
            }
            i++;
        }
        return objet;

    }

    // initialiser un objet a partir des parametre
    public Object initialize_from_param(HttpServletRequest req, HttpServletResponse res, Class<?> objet_param)
            throws NumberFormatException, IllegalArgumentException, IllegalAccessException, InstantiationException,
            IOException {
        Field[] liste_field = objet_param.getDeclaredFields();
        Object objet = objet_param.newInstance();

        for (Field field : liste_field) {
            field.setAccessible(true);
            if (field.getType() == int.class) {
                field.set(objet, Integer.parseInt(req.getParameter(field.getName())));
            } else if (field.getType() == String.class) {
                field.set(objet, req.getParameter(field.getName()));
            } else {
                throw new InvalidParameterException("le parametre inserer doit etre de type string ou int");
            }
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

    /*
     * fonction qui initialize le session
     */
    private void initialize_session(Object objet, HttpServletRequest request) throws Exception {
        Field[] att = objet.getClass().getDeclaredFields();
        for (Field f : att) {
            if (f.getType() == Session.class) {
                f.setAccessible(true);
                f.set(objet, new Session(request.getSession()));
            }
        }
    }

}