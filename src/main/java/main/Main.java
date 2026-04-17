package main;

import tools.Mydb;
import java.sql.Connection;

public class Main {
    public static void main(String[] args) {
        Mydb mydb = Mydb.getInstance();
        Connection cnx = mydb.getConnection();

        if (cnx != null) {
            System.out.println("Connexion OK");
        } else {
            System.out.println("Connexion échouée");
        }
    }
}