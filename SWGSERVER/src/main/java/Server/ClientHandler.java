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
    private String userId;
    
    private CommandFactory commandFactory;
    
    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.sessionManager = SessionManager.getInstance();
        this.loginProcessor = new LoginProcessor();
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
            this.commandFactory = new CommandFactory(this.socket, out, sessionManager, loginProcessor);
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
                // ─── 로그아웃 처리 ────────────────────────────────────
                if (msg.equals("LOGOUT")) {
                    System.out.println("로그아웃 요청 수신: " + userId);
                    sessionManager.logout(userId);  // 세션에서 제거
                    break; // 스레드 종료
                }
                
                // ─── 로그인 처리 ─────────────────────────────────────
                // ─── 텍스트 파일 동기화 처리 ──────────────────────────                
                // ─── 백업 처리 ───────────────────────────────────
                // ─── 사용자 정보 처리 ───────────────────────────────────
                // ─── 사용자 정보 요청 처리 ────────────────────────────
                // ─── 사용자 예약 취소 알람 처리 ───────────────────────────────────
                Command command = commandFactory.getCommand(msg);
                if (command != null) {
                    command.execute(msg, in);
                    
                    if (msg.startsWith("LOGIN:")) {
                        this.userId = commandFactory.getUserId();
                    }
                } else {
                    System.out.println("[서버] 알 수 없는 요청:" + msg);
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
