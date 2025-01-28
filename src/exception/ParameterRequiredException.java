package exception;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import annotation.ErrorPage;
import annotation.Required;

public class ParameterRequiredException extends Exception {

    String param_name;
    private Field[] liste_field;
    private Object objet;
    Method method;

    public ParameterRequiredException(String message) {
        super(message);
    }

    public ParameterRequiredException(Field[] liste_field, Object o, Method m) {
        this.liste_field = liste_field;
        this.objet = o;
        this.method = m;
    }

    /**
     * @return the objet
     */
    public Object getObjet() {
        return objet;
    }

    /**
     * @param objet the objet to set
     */
    public void setObjet(Object objet) {
        this.objet = objet;
    }

    /**
     * @return the liste_field
     */
    public Field[] getListe_field() {
        return liste_field;
    }

    /**
     * @param liste_field the liste_field to set
     */
    public void setListe_field(Field[] liste_field) {
        this.liste_field = liste_field;
    }

    /**
     * @return the param_name
     */
    public String getParam_name() {
        return param_name;
    }

    /**
     * @param param_name the param_name to set
     */
    public void setParam_name(String param_name) {
        this.param_name = param_name;
    }

    public String afficherHTMLRedirectToError() throws IllegalArgumentException, Exception {
        ErrorPage error_page = method.getAnnotation(ErrorPage.class);
        String url = error_page.value();
        String method = error_page.method();

        String error = "";
        String data = "";
        for (Field field : liste_field) {
            boolean issetError = false;
            if (field.isAnnotationPresent(Required.class)) {
                if (field.get(objet) == null) {
                    issetError = true;
                } else {
                    if ((field.get(objet) + "").isEmpty()) {
                        issetError = true;
                    }
                }
            }

            if (issetError) {
                error += "<input type='hidden' value='' name='__" + field.getName() + "'/>";
            } else {
                // if (field.get(objet) != null) {
                data += "<input type='hidden' value='" + field.get(objet) + "' name='" +
                        field.getName() + "'/>";
                // }
            }
        }

        return "<!DOCTYPE html>\n" + //
                "<html lang=\"en\">\n" + //
                "<head>\n" + //
                "    <meta charset=\"UTF-8\">\n" + //
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" + //
                "    <title>Document</title>\n" + //
                "</head>\n" + //
                "<script>\n" + //
                "    window.onload = function(){\n" + //
                "        document.getElementById(\"formulaire\").submit();\n" + //
                "    }\n" + //
                "</script>\n" + //
                "<body>\n" + //
                "    <form action=\"" + url + "\" method=\"" + method + "\" id=\"formulaire\" >" +
                error + data +
                "</form>\n" + //
                "</body>\n" + //
                "</html>";
    }

}
