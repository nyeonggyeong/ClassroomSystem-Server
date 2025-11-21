/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.LoginManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author user
 */
public class LoginCommand implements Command {
    private LoginManager loginManager;
    
    public LoginCommand(LoginManager loginManager) {
        this.loginManager = loginManager;
    }

    @Override
    public void execute(String message, BufferedReader in) throws IOException {
        loginManager.login(message);
    }
    
}
