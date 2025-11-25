/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import Client.LoginManager;
import java.io.BufferedReader;
import java.io.IOException;

/**
 *
 * @author user
 */
public class FindPasswordCommand implements Command{
    private LoginManager loginManager;
    
    public FindPasswordCommand(LoginManager loginManager) {
        this.loginManager = loginManager;
    }
    @Override
    public void execute(String message, BufferedReader in) throws IOException {
        System.out.println("[서버] 사용자 비밀번호 찾기 처리");
        String pwdData = message.substring("FIND_PASSWORD:".length());
        
        if (pwdData.isEmpty()) {
            System.out.println("[서버] 데이터가 없음");
            return;
        }
        loginManager.checkPassword(pwdData);
        
    }
    
}
