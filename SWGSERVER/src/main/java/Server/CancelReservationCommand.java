/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

/**
 *
 * @author user
 */
public class CancelReservationCommand implements Command {
    private SessionManager sessionManager;
    
    public CancelReservationCommand(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }
    @Override
    public void execute(String message, BufferedReader in) throws IOException {
        System.out.println("[서버] 사용자 예약 취소 알람 처리");
        String userDatas = message.substring("CANCEL_RESERVATION:".length());

        if (userDatas.isEmpty()) {
            System.out.println("[서버] 취소 사용자 없음");
            return;
        }
        String[] userData = userDatas.split(";");
        for (String data : userData) {
            if (!data.isEmpty()) {
                sessionManager.addCancelUser(data);
            }
        }
    }
    
}
