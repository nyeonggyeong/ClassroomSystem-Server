/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.RegisterHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author user
 */
public class RegisterCommand implements Command{
    private RegisterHandler registerHandler;
    
    public RegisterCommand(RegisterHandler registerHandler) {
        this.registerHandler = registerHandler;
    }
    @Override
    public void execute(String message, BufferedReader in) throws IOException {       
        registerHandler.handle(message);        
    }
}
