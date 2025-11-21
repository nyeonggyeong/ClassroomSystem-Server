/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Server.SessionManager;
import Server.LoginProcessor;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author user
 */
public class LoginManager {
    private BufferedWriter out;
    private LoginProcessor loginProcessor;
    private SessionManager sessionManager;
    private String userId;
    
    public LoginManager(LoginProcessor loginProcessor, SessionManager sessionManager, BufferedWriter out) {
        this.loginProcessor = loginProcessor;
        this.sessionManager = sessionManager;
        this.out = out;
    }
    public void login(String message) throws IOException {
        System.out.println("[서버] 로그인 요청: " + message);
        String[] parts = message.substring("LOGIN:".length()).split(",");
        if (parts.length < 3) {
            out.write("FAIL");
            out.newLine();
            out.flush();            
        }

        userId = parts[0].trim();
        String password = parts[1].trim();
        String role = parts[2].trim();

        boolean valid = loginProcessor.validateLogin(userId, password, role);
        System.out.println("[서버] 로그인 검증 결과: " + valid);
        if (!valid) {
            out.write("FAIL");
            out.newLine();
            out.flush();
            System.out.println("[서버] 응답: FAIL");            
        }

        if ("admin".equalsIgnoreCase(role)) {
            out.write("LOGIN_SUCCESS");
            out.newLine();
            out.flush();
            System.out.println("[서버] 응답: LOGIN_SUCCESS (admin)");
        }

//                    SessionManager.PendingClient pending
//                            = new SessionManager.PendingClient(socket, userId, out);
        SessionManager.LoginDecision result
                = loginProcessor.tryUserLogin(userId, out);

        if (result == SessionManager.LoginDecision.OK) {
//                        List<String> usersData = sessionManager.checkUser();
            out.write("LOGIN_SUCCESS");
            out.newLine();
            out.flush();
            System.out.println("[서버] 응답: LOGIN_SUCCESS (user)");
            List<String> pendingNotifications = sessionManager.getPendingCancelNotifications(userId);
            if (!pendingNotifications.isEmpty()) {
                StringBuilder cancelMsg = new StringBuilder("CANCEL_NOTIFICATION:");
                for (int i = 0; i < pendingNotifications.size(); i++) {
                    if (i > 0) {
                        cancelMsg.append(";");
                    }
                    cancelMsg.append(pendingNotifications.get(i));
                }

                out.write(cancelMsg.toString());
                out.newLine();
                out.flush();
                System.out.println("[서버] 메시지: " + cancelMsg.toString());
                System.out.println("[서버] 로그인 후 대기 중이던 취소 알림 전송: " + userId);
            }
        } else if (result == SessionManager.LoginDecision.WAIT) {
            out.write("WAIT");
            out.newLine();
            out.flush();
            System.out.println("[서버] 응답: WAIT (queued)");
        } else {
            out.write("FAIL");
            out.newLine();
            out.flush();
            System.out.println("[서버] 응답: FAIL");
        }
    }
    
    public String getUserId() { return userId; }
}
