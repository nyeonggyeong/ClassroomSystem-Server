/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package Server;

import org.junit.jupiter.api.*;
import java.io.*;
import java.lang.reflect.Field;
import java.net.Socket;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class SessionManagerTest {

    private SessionManager manager;

    @BeforeEach
    public void setUp() throws Exception {
        manager = SessionManager.getInstance();
        
        resetSessionManager();
    }

    private void resetSessionManager() throws Exception {
        // Field 객체에 SessionManager 클래스에 이름이 active인 필드 정보를 저장
        Field activeField = SessionManager.class.getDeclaredField("active");
        // 접근 제어 무력화
        activeField.setAccessible(true);
        // manager 객체가 가지고있는 active 필드 값을 가져와 저장
        Set<String> active = (Set<String>) activeField.get(manager);
        active.clear();

        Field queueField = SessionManager.class.getDeclaredField("queue");
        queueField.setAccessible(true);
        // Queue 안에 어떤 타입이 들어있는지 명확하지 않거나 중요하지 않아 와일드카드 ?를 사용
        Queue<?> queue = (Queue<?>) queueField.get(manager);
        queue.clear();

        Field cancelUsersField = SessionManager.class.getDeclaredField("cancelUsers");
        cancelUsersField.setAccessible(true);
        Set<String> cancelUsers = (Set<String>) cancelUsersField.get(manager);
        cancelUsers.clear();

        Field userStreamsField = SessionManager.class.getDeclaredField("userStreams");
        userStreamsField.setAccessible(true);
        Map<?, ?> userStreams = (Map<?, ?>) userStreamsField.get(manager);
        userStreams.clear();
    }
    
    @Test
    public void testLogin() throws IOException {
        BufferedWriter dummyOut1 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter dummyOut2 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter dummyOut3 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter dummyOut4 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter dummyOut5 = new BufferedWriter(new OutputStreamWriter(System.out));
        
        SessionManager.PendingClient client1 = new SessionManager.PendingClient("user1", dummyOut1);
        SessionManager.PendingClient client2 = new SessionManager.PendingClient("user2", dummyOut2);
        SessionManager.PendingClient client3 = new SessionManager.PendingClient("user3", dummyOut3);
        SessionManager.PendingClient client4 = new SessionManager.PendingClient("user4", dummyOut4);
        SessionManager.PendingClient client5 = new SessionManager.PendingClient("user5", dummyOut5);
        
        SessionManager.LoginDecision result1 = manager.tryLogin("user1", client1);
        SessionManager.LoginDecision result2 = manager.tryLogin("user2", client2);
        SessionManager.LoginDecision result3 = manager.tryLogin("user3", client3);
        SessionManager.LoginDecision result4 = manager.tryLogin("user4", client4);
        SessionManager.LoginDecision result5 = manager.tryLogin("user5", client5);
        
        Assertions.assertEquals(SessionManager.LoginDecision.OK, result1);
        Assertions.assertEquals(SessionManager.LoginDecision.OK, result2);
        Assertions.assertEquals(SessionManager.LoginDecision.OK, result3);
        Assertions.assertEquals(SessionManager.LoginDecision.OK, result4);
        Assertions.assertEquals(SessionManager.LoginDecision.WAIT, result5);
    }
    
    @Test
    public void testLogoutAndNextClient() throws IOException {
        BufferedWriter out1 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter out2 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter out3 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter out4 = new BufferedWriter(new OutputStreamWriter(System.out));
        BufferedWriter out5 = new BufferedWriter(new OutputStreamWriter(System.out));
        
        // 4명 로그인
        manager.tryLogin("user1", new SessionManager.PendingClient("user1", out1));
        manager.tryLogin("user2", new SessionManager.PendingClient("user2", out2));
        manager.tryLogin("user3", new SessionManager.PendingClient("user3", out3));
        manager.tryLogin("user4", new SessionManager.PendingClient("user4", out4));
        
        // 5번째는 대기
        SessionManager.LoginDecision result5 = manager.tryLogin("user5", 
            new SessionManager.PendingClient("user5", out5));
        Assertions.assertEquals(SessionManager.LoginDecision.WAIT, result5);
        
        // user1 로그아웃 -> user5 자동 로그인 (nextClient 호출됨)
        manager.logout("user1");
        
        // user5가 다시 로그인 시도하면 중복으로 처리되어야 함 (이미 자동 로그인됨)
        SessionManager.LoginDecision result5Again = manager.tryLogin("user5", 
            new SessionManager.PendingClient("user5", out5));
        Assertions.assertEquals(SessionManager.LoginDecision.FAIL_DUP, result5Again);
    }
    @Test
    public void testCancelNotification() throws IOException {
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));
        SessionManager.PendingClient client = new SessionManager.PendingClient("user1", out);
        
        // user1 로그인
        manager.tryLogin("user1", client);
        
        // 예약 취소 데이터 추가
        String cancelData = "reservationId123,user1,2024-12-25";
        manager.addCancelUser(cancelData);
        
        // 실제로는 BufferedWriter를 통해 전송되지만, 
        // 여기서는 메서드가 정상 실행되는지 확인
        Assertions.assertDoesNotThrow(() -> {
            manager.addCancelUser(cancelData);
        });
    }
}
