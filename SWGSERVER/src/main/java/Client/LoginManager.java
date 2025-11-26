/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Client;

import Server.SessionManager;
import Server.LoginProcessor;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
    private static final String USER_PATH = "src/main/resources/USER_LOGIN.txt";
    private static final String PROFESSOR_PATH = "src/main/resources/PROFESSOR_LOGIN.txt";
    private static final String ADMIN_PATH = "src/main/resources/ADMIN_LOGIN.txt";
    
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
            return;
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
            return;
        }

        if ("admin".equalsIgnoreCase(role)) {
            out.write("LOGIN_SUCCESS");
            out.newLine();
            out.flush();
            System.out.println("[서버] 응답: LOGIN_SUCCESS (admin)");
            return;
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
    
    public void checkPassword(String data) {
        System.out.println("[서버] 비밀번호를 찾겠습니다.");
        String[] datas = data.split(",");
        String path = "";
        if (datas[2].equals("user")) {
            path = USER_PATH;
        } else if (datas[2].equals("professor")) {
            path = PROFESSOR_PATH;
        } else {
            path = ADMIN_PATH;
        }
        String userId = datas[0];
        String pwd = getPassword(path, userId);
        
        if (pwd == null || pwd.isEmpty()) {
            StringBuilder sb = new StringBuilder("FIND_PASSWORD:");
            sb.append("NOT_FOUND");
            System.out.println("비밀번호: " + sb.toString());
            try {
                out.write(sb.toString());
                out.newLine();
                out.flush();
            } catch (IOException ex) {
                System.out.println("에러 발생: " + ex.getMessage());
            } 
        } else if (!pwd.isEmpty()) {
            StringBuilder sb = new StringBuilder("FIND_PASSWORD:");
            sb.append(pwd);
            System.out.println("비밀번호: " + sb.toString());
            try {
                out.write(sb.toString());
                out.newLine();
                out.flush();
            } catch (IOException ex) {
                System.out.println("에러 발생: " + ex.getMessage());
            } 
        }
    }
    
    public String getPassword(String path, String userId) {
        System.out.println("[서버] 파일을 열어 찾습니다.");
        try (BufferedReader reader = new BufferedReader(
                                            new InputStreamReader(
                                                new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equals(userId)) {
                    System.out.println("[서버] 찾았습니다.");
                    return parts[1];
                }
            }
            return null;
        } catch (IOException e) {
            System.out.println("에러 발생: " + e.getMessage());     
            return null;
        }
    }
}
