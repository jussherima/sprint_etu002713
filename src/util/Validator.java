package util;

import jakarta.servlet.http.HttpServletRequest;

public class Validator {
    HttpServletRequest req;

    public Validator(HttpServletRequest req) {
        this.req = req;
    }

    public String showErrorFor(String columname) {
        if (req.getParameter("__" + columname) == null) {
            return "";
        } else {
            return "le champ " + columname + " est obligatoir";
        }
    }

    public String getValueFor(String columname) {
        if (req.getParameter(columname) == null) {
            return "";
        } else {
            return req.getParameter(columname);
        }
    }

}
