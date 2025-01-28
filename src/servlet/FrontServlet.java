package servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import annotation.Get;
import annotation.Post;
import annotation.RequestBody;
import annotation.Required;
import annotation.URL;
import exception.ControllerFolderNotFoundException;
import exception.DuplicateUrlException;
import exception.InvalidParameterException;
import exception.InvalideFunctionRetourException;
import exception.NoSuchUrlExcpetion;
import exception.ParameterRequiredException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import util.FilePart;
import util.Mapping;
import util.ModelView;
import util.Session;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 10, maxRequestSize = 1024 * 1024 * 20)
public class FrontServlet extends HttpServlet {

    List<String> liste_controller;
    HashMap<String, Mapping[]> mon_map;
    Exception error = null;
    List<String> liste_nom = new ArrayList<>();
    Class<?> method_appeler;
    int status = 200;
    // ModelView previewsModelView;

    // intialization du servlet
    @SuppressWarnings("unchecked")
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        liste_controller = new ArrayList<>();
        mon_map = new HashMap<>(); // contient tous les method avec leur controller

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
        this.method_appeler = Get.class;
        processRequest(request, response);

    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.method_appeler = Post.class;
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
                response.setStatus(status);
                invoke_method(path, request, response);
                response.setStatus(200);
            } catch (NoSuchUrlExcpetion e) {
                erreur(out, 2, e);
                response.setStatus(404);
            } catch (ParameterRequiredException e) {

                response.setContentType("text/html");
                try {
                    response.getWriter().print(e.afficherHTMLRedirectToError());
                } catch (Exception err) {
                    e.printStackTrace(out);
                }

            } catch (Exception e) {
                response.setStatus(500);
                out.println("misy erreur le izy:" + e.getMessage());
                e.printStackTrace(out);
            }
        } else {
            if (error instanceof ControllerFolderNotFoundException) {
                erreur(out, 2);
                response.setStatus(500);
            } else if (error instanceof DuplicateUrlException) {
                response.setStatus(500);
                erreur(out, 1);
            } else {
                response.setStatus(500);
                out.println(error.getMessage());
            }
        }
    }

    // proceder l'url lannotation
    @SuppressWarnings({ "unused", "rawtypes", "unchecked" })
    private void invoke_method(String url, HttpServletRequest req, HttpServletResponse res)
            throws NoSuchUrlExcpetion, InvalideFunctionRetourException, Exception {
        boolean url_existe = false;
        String annotation_est_present = "tsia";
        // verifie si tous c'est bien passer
        boolean noerror = false;

        // boucler tous les controlleurs
        for (Map.Entry<String, Mapping[]> entry : this.mon_map.entrySet()) {
            annotation_est_present += " | " + entry.getKey() + " | ";
            // prendre une valeur du <l'url,mapping associe>
            String valeur_url = entry.getKey();
            // verifie si c'est l'url que l'on cherche
            if (valeur_url.equals(url)) {
                for (Mapping mp : entry.getValue()) {
                    // verfie si les parametres sont tous annoters
                    if (mp.getArgument().containsKey("error")) {
                        throw new Exception("ETU 002713 erreur pas de annotation detecter");
                    }

                    url_existe = true;
                    // prendre la class avec son nom
                    Class<?> clazz = Class.forName(mp.getClassName());
                    Method m = null;

                    // prendre une instance de la method qui correspend a l'url
                    try {
                        m = clazz.getDeclaredMethod(mp.getMethodName(),
                                mp.method_param());
                    } catch (Exception e) {
                        throw new Exception(
                                "exception trouver sur la method invoke_method sur le get DeclaredMethod"
                                        + "<br/>le nom de la class est " + clazz.getName()
                                        + "<br/>le nom de la method est " + mp.getMethodName()
                                        + "<br/>la longeur du parametre est " + mp.method_param().length
                                        + mp.method_param()[0].getName());
                    }

                    @SuppressWarnings("deprecation")

                    // minstancer an'le session ao anaty objet
                    Object objet = clazz.newInstance();
                    initialize_session(objet, req);

                    // verifier si le method est bien annoter selon le verb(ex:post ou get) que l'on
                    // cherche
                    if (m.isAnnotationPresent((Class<? extends Annotation>) method_appeler)) {

                        // invoquer l'objet et prendre sa valeur de retour
                        Object retour = m.invoke(objet,
                                get_request_param(req, res, mp.getArgument(), m));

                        // dans le cas ou le retour de la method est une string
                        if (retour.getClass() == String.class) {
                            noerror = true;
                            res.getWriter().println((String) retour);
                        }
                        // dans le cas ou notre retour est une ModelView
                        else if (retour.getClass() == ModelView.class) {
                            res.getWriter().println("l'instance de la class est une view");
                            noerror = true;
                            trait_view(req, res, (ModelView) retour);
                        }
                        // dans le cas ou on veut faire des rest api
                        else {
                            res.setContentType("application/json");
                            Gson gson = new Gson();
                            String retour_json = gson.toJson(retour);
                            noerror = true;
                            res.getWriter().println(retour_json);
                        }
                        break;
                    }
                }
            }

        }

        if (!url_existe) {
            // res.getWriter().println("aucun method associer a cette url");
            throw new NoSuchUrlExcpetion("l'url que vous avez saisie n'existe pas");

        } else {
            if (!noerror) {
                throw new Exception("ne peut pas etre appeler par cette verb " + annotation_est_present);
            }
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
    private HashMap<String, Mapping[]> getAnnoteMethods(Class<?> my_class) throws DuplicateUrlException, Exception {
        Method[] liste_method = my_class.getDeclaredMethods();
        HashMap<String, Mapping[]> liste_annoted_method = new HashMap<>();
        for (Method m : liste_method) {

            // verifier si la method est annoter
            if (m.isAnnotationPresent(URL.class)) {
                String my_url = m.getAnnotation(URL.class).url();

                // verifie si l'url est deja definie avec le meme verb
                if (liste_annoted_method.containsKey(my_url)) {
                    for (Mapping mp : liste_annoted_method.get(my_url)) {
                        if (m.isAnnotationPresent(mp.getVerb())) {
                            throw new DuplicateUrlException(
                                    "l'url " + my_url + " contient 2 fonction en meme temps avec verb = "
                                            + mp.getVerb());
                        }
                    }
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

                // prendre le verb de la fonction
                Class method_verb = Get.class;
                if (!m.isAnnotationPresent(method_verb)) {
                    method_verb = Post.class;
                }

                // verifie si l'url existe deja
                if (liste_annoted_method.containsKey(my_url)) {
                    Mapping[] mapping = new Mapping[2];
                    mapping[0] = liste_annoted_method.get(my_url)[0];
                    mapping[1] = new Mapping(my_class.getName(), m.getName(), method_liste, method_verb);
                    liste_annoted_method.put(my_url, mapping);
                } else {
                    Mapping[] mapping = new Mapping[1];
                    mapping[0] = new Mapping(my_class.getName(), m.getName(), method_liste, method_verb);
                    liste_annoted_method.put(my_url, mapping);

                }
            }
        }
        return liste_annoted_method;
    }

    // fonction pour prendre tous les valeurs dans la requette
    public Object[] get_request_param(HttpServletRequest request, HttpServletResponse response,
            HashMap<String, Class<?>> liste_objet, Method method)
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
            // initaliser la valeur integer du parametre
            else if (entry.getValue() == int.class) {
                try {
                    objet[i] = Integer.parseInt(request.getParameter(entry.getKey()));
                } catch (Exception e) {
                    objet[i] = "null";
                }
            }
            // initialiser la valeur double
            else if (entry.getValue() == double.class) {
                try {
                    objet[i] = Double.parseDouble(request.getParameter(entry.getKey()));
                } catch (Exception e) {
                    objet[i] = "null";
                }
            }
            // intitialiser une valeur filepart
            else if (entry.getValue() == FilePart.class) {
                try {
                    Part filepart = request.getPart(entry.getKey());
                    String filename = filepart.getSubmittedFileName();
                    byte[] liste_byte;
                    try (InputStream reader = filepart.getInputStream()) {
                        try (ByteArrayOutputStream byteArray = new ByteArrayOutputStream()) {
                            byte[] buffer = new byte[1024];
                            int bytesread;
                            while ((bytesread = reader.read(buffer)) != -1) {
                                byteArray.write(buffer, 0, bytesread);
                            }
                            liste_byte = byteArray.toByteArray();
                        }

                    }
                    FilePart fp = new FilePart(filename, liste_byte);
                    objet[i] = fp;
                } catch (Exception e) {
                    throw new Exception("erreur pendant l'upload du fichier " + e.getMessage());
                }
            }
            // initialiser les objet
            else {
                Class<?> objet_param = entry.getValue();
                try {
                    objet[i] = initialize_from_param(request, response, objet_param, method);
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
    public Object initialize_from_param(HttpServletRequest req, HttpServletResponse res, Class<?> objet_param,
            Method metod)
            throws NumberFormatException, IllegalArgumentException, IllegalAccessException, InstantiationException,
            IOException, InvalidParameterException, ParameterRequiredException, NoSuchFieldException,
            SecurityException {
        Field[] liste_field = objet_param.getDeclaredFields();
        Object objet = objet_param.newInstance();

        // JFrame f = new JFrame();

        boolean issetError = false;

        for (Field field : liste_field) {
            field.setAccessible(true);
            String param_value = req.getParameter(field.getName());

            // JOptionPane.showMessageDialog(f, "field name = " + field.getName() + " et
            // parm value " + param_value,
            // "message", 1);

            if (field.isAnnotationPresent(Required.class) && ((param_value == null) || param_value.isEmpty())) {
                issetError = true;
                // JOptionPane.showMessageDialog(f, "isset error", "message", 1);
            } else {
                // initializer les parametre integer
                if (field.getType() == int.class) {
                    field.set(objet, Integer.parseInt(param_value));
                }
                // initializer les valeurs double
                else if (field.getType() == double.class) {
                    field.set(objet, Double.parseDouble(param_value));
                }
                // initializer les parametre string
                else if (field.getType() == String.class) {
                    field.set(objet, param_value);
                } else {
                    throw new InvalidParameterException("le parametre inserer doit etre de type string ou int");
                }
            }

        }

        if (issetError) {
            throw new ParameterRequiredException(liste_field, objet, metod);
        }

        // String nom = (String) objet.getClass().getDeclaredField("nom").get(objet);
        // JOptionPane.showMessageDialog(f, "param value =" + nom, "message", 1);

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

    private void status_404() {

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