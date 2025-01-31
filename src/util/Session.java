package util;

import authentification.UserInterface;
import jakarta.servlet.http.HttpSession;

public class Session {
    HttpSession sess;

    public Session(HttpSession sess) {
        this.sess = sess;
    }

    public void add(String name, Object objet) {
        sess.setAttribute(name, objet);
    }

    public void remove(String name) {
        sess.removeAttribute(name);
    }

    public Object get(String name) {
        return sess.getAttribute(name);
    }

    public void logUser(UserInterface user) {
        add("utilisateur_logged", user);
    }
}
