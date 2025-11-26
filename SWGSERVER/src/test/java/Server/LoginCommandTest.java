/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Client.LoginManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginCommandTest {

    @Mock
    LoginManager loginManager;

    @Mock
    BufferedReader bufferedReader; // 인터페이스 규격용 (실제 사용 안 함)

    @Test
    @DisplayName("로그인 명령 실행 시 - LoginManager.login()이 호출되어야 함")
    void execute_ShouldDelegateToLoginManager() throws IOException {
        // Given
        LoginCommand command = new LoginCommand(loginManager);
        String loginMessage = "LOGIN:id,password";

        // When
        command.execute(loginMessage, bufferedReader);

        // Then
        // execute 메서드 내부에서 loginManager.login(message)가 정확히 호출되었는지 검증
        verify(loginManager, times(1)).login(loginMessage);
    }
}
