/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.UserInfoHandler;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.Socket;

/**
 *
 * @author user
 */
public class UserInfoCommand implements Command {
    private UserInfoHandler userInfoHandler;
   
    public UserInfoCommand(UserInfoHandler userInfoHandler) {
        this.userInfoHandler = userInfoHandler;
    }

    @Override
    public void execute(String message, BufferedReader in) throws IOException {
        System.out.println("[서버] 사용자 정보 처리");
        userInfoHandler.getUserInfo();        
    }
    
}
