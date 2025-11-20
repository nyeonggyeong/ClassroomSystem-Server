/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import Client.FileSyncManager;
import java.io.*;
import java.net.Socket;

import Client.RegisterHandler;     // 서버 쪽 핸들러
import Client.UserInfoHandler;     // 서버 쪽 핸들러
import java.util.List;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final SessionManager sessionManager;
    private LoginProcessor loginProcessor;
    private BufferedReader in;
    private BufferedWriter out;
    private String userId = null;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.sessionManager = SessionManager.getInstance();
        this.loginProcessor = new LoginProcessor();
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            System.out.println("스트림 초기화 실패: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String msg;
            String input = null;

            while ((msg = in.readLine()) != null) {
                // ─── 회원가입 처리 ───────────────────────────────────
                if (msg.startsWith("REGISTER:")) {
                    RegisterHandler regHandler = new RegisterHandler(out);
                    regHandler.handle(msg);
                    continue;
                }

                // ─── 사용자 정보 요청 처리 ────────────────────────────
                if (msg.startsWith("INFO_REQUEST:")) {
                    UserInfoHandler infoHandler = new UserInfoHandler(socket, out);
                    infoHandler.handle(msg);
                    System.out.println("사용자 요청 처리");
                    continue;
                }

                // ─── 로그인 처리 ─────────────────────────────────────
                if (msg.startsWith("LOGIN:")) {
                    System.out.println("[서버] 로그인 요청: " + msg);
                    String[] parts = msg.substring("LOGIN:".length()).split(",");
                    if (parts.length < 3) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
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
                        continue;
                    }

                    if ("admin".equalsIgnoreCase(role)) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("[서버] 응답: LOGIN_SUCCESS (admin)");
                        continue;
                    }

//                    SessionManager.PendingClient pending
//                            = new SessionManager.PendingClient(socket, userId, out);
                    SessionManager.LoginDecision result
                            = loginProcessor.tryUserLogin(userId, socket, out);

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
                    continue;
                }
                // ─── 텍스트 파일 동기화 처리 ──────────────────────────
                if (msg.startsWith("FILE_UPDATE:")) {
                    String filename = msg.substring("FILE_UPDATE:".length()).trim();
                    StringBuilder content = new StringBuilder();
                    String line;
                    while (!(line = in.readLine()).equals("<<EOF>>")) {
                        content.append(line).append("\n");
                    }

                    FileSyncManager manager = new FileSyncManager();
                    try {
                        manager.updateFile(filename, content.toString());
                        System.out.println("[서버] 파일 동기화 완료: " + filename);
                    } catch (IOException e) {
                        System.err.println("[서버] 파일 동기화 실패: " + filename);
                        e.printStackTrace();
                    }
                    continue;
                }
                // ─── 백업 처리 ───────────────────────────────────
                if (msg.equals("BACKUP_REQUEST")) {
                    System.out.println("[서버] 백업 요청");

                    FileSyncManager manager = new FileSyncManager();
                    String result = manager.createBackup();

                    out.write(result);
                    out.newLine();
                    out.flush();
                    System.out.println("[서버] 백업 응답: " + result);
                    continue;
                }
                // ─── 사용자 예약 취소 알람 처리 ───────────────────────────────────
                if (msg.startsWith("CANCEL_RESERVATION:")) {
                    System.out.println("[서버] 사용자 예약 취소 알람 처리");
                    String userDatas = msg.substring("CANCEL_RESERVATION:".length());

                    if (userDatas.isEmpty()) {
                        System.out.println("[서버] 취소 사용자 없음");
                        continue;
                    }
                    String[] userData = userDatas.split(";");
                    for (String data : userData) {
                        if (!data.isEmpty()) {
                            sessionManager.addCancelUser(data);
                        }
                    }
                    continue;

                }
                // ─── 사용자 정보 처리 ───────────────────────────────────
                if (msg.equals("USER_INFO")) {
                    System.out.println("[서버] 사용자 정보 처리");
                    UserInfoHandler userInfo = new UserInfoHandler(socket, out);
                    userInfo.getUserInfo();
                    continue;
                }
                // ─── 로그아웃 처리 ────────────────────────────────────
                if (msg.equals("LOGOUT")) {
                    System.out.println("로그아웃 요청 수신: " + userId);
                    sessionManager.logout(userId);  // 세션에서 제거
                    break; // 스레드 종료
                }
            }
        } catch (IOException e) {
            System.out.println("[서버] 강제종료");
        } finally {
            try {
                if (userId != null) {
                    sessionManager.logout(userId);
                    System.out.println("[서버] 세션 정리: " + userId);
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("[서버] 종료 ");
            }
        }
    }
}
