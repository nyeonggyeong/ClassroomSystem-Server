package Server;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 * 싱글턴 패턴 사용
 *
 * @author adsd3
 */
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.*;

public class SessionManager {

    private static final int MAX_USER = 4;
    private final int maxUsers;
    private final Set<String> active = new HashSet<>();  // 사용자만 저장
    private final Queue<PendingClient> queue = new ArrayDeque<>(); // 대기 사용자 저장
    private final Set<String> cancelUsers;
    private final Map<String, BufferedWriter> userStreams = new HashMap<>();

    private static final SessionManager uniqueInstance = new SessionManager(MAX_USER);

    private SessionManager(int maxUsers) {
        this.maxUsers = maxUsers;
        this.cancelUsers = new HashSet<>();
    }

    public enum LoginDecision {
        OK, WAIT, FAIL_DUP
    }

    // 대기 사용자 클래스
    public static class PendingClient {
        public final String userId;
        public final BufferedWriter out;

        public PendingClient(String userId, BufferedWriter out) {
            this.userId = userId;
            this.out = out;
        }
    }

    public static SessionManager getInstance() {
        return uniqueInstance;
    }

    public synchronized void addCancelUser(String data) {
        if (data != null && !data.isEmpty()) {
            if (!cancelUsers.contains(data)) {
                cancelUsers.add(data);
            }
            String[] parts = data.split(",");
            if (parts != null && parts.length >= 2) {
                String userId = parts[1];
                BufferedWriter userOut = userStreams.get(userId);

                if (userOut != null) {
                    try {
                        userOut.write("CANCEL_NOTIFICATION:" + data);
                        userOut.newLine();
                        userOut.flush();
                        System.out.println("[서버] 예약 취소 알림 전송");

                        cancelUsers.remove(data);
                    } catch (IOException e) {
                        System.out.println("에러발생: " + e.getMessage());
                    }
                } else {
                    System.out.println("[서버] 사용자 현재 로그아웃 상태");
                }
            }
        }
    }

    public synchronized List<String> checkUser() {
        if (cancelUsers.isEmpty()) {
            return null;
        }

        Iterator<String> user = cancelUsers.iterator();
        List<String> result = new ArrayList<>();

        while (user.hasNext()) {
            String userData = user.next();

            String[] data = userData.split(",");
            if (active.contains(data[1])) {
                result.add(userData);
                user.remove();
            }
        }
        return result;
    }

    public synchronized LoginDecision tryLogin(String userId, PendingClient pending) {
        // 아이디 만으로 중복 로그인을 시도하면 동일 아이디에 대한 제한이 있어야하거나, 조건이 더 필요
        if (active.contains(userId)) {
            System.out.println("중복 로그인 시도 감지: " + userId);
            return LoginDecision.FAIL_DUP;
        }
        if (active.size() < maxUsers) {
            active.add(userId); // 사용자만 저장
            userStreams.put(userId, pending.out);

            return LoginDecision.OK;
        } else {
            queue.offer(pending);
            return LoginDecision.WAIT;
        }
    }

    public synchronized List<String> getPendingCancelNotifications(String userId) {
        List<String> result = new ArrayList<>();
        Iterator<String> iterator = cancelUsers.iterator();

        while (iterator.hasNext()) {
            String userData = iterator.next();
            String[] parts = userData.split(",");

            if (parts.length >= 2 && parts[1].equals(userId)) {
                result.add(userData);
                iterator.remove();
            }
        }
        return result;
    }

    public synchronized void logout(String userId) {
        active.remove(userId);  // 사용자만 제거
        userStreams.remove(userId);
        nextClient();
    }

    public synchronized void nextClient() {
        if (active.size() >= maxUsers || queue.isEmpty()) {
            return;
        }

        PendingClient next = queue.poll();
        try {
            next.out.write("LOGIN_SUCCESS");
            next.out.newLine();
            next.out.flush();
            active.add(next.userId);
            userStreams.put(next.userId, next.out);
            System.out.println("대기자 자동 로그인: " + next.userId);

            List<String> pendingNotifications = getPendingCancelNotifications(next.userId);
            if (!pendingNotifications.isEmpty()) {
                StringBuilder msg = new StringBuilder("CANCEL_NOTIFICATION:");
                for (int i = 0; i < pendingNotifications.size(); i++) {
                    if (i > 0) {
                        msg.append(";");
                    }
                    msg.append(pendingNotifications.get(i));
                }

                next.out.write(msg.toString());
                System.out.println("[서버] 메시지: " + msg.toString());
                next.out.newLine();
                next.out.flush();
                System.out.println("[서버] 대기자 로그인 후 취소 알림 전송: " + next.userId);
            }
        } catch (Exception e) {
            System.err.println("대기자 로그인 실패: " + e.getMessage());
        }
    }
}
