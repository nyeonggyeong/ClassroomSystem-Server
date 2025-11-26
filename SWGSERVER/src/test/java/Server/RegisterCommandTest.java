/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Client.RegisterHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCommandTest {

    @Mock
    RegisterHandler registerHandler;

    @Mock
    BufferedReader bufferedReader; // 인터페이스 규격용 (실제 사용 안 함)

    @Test
    @DisplayName("회원가입 요청 실행 시 - RegisterHandler.handle()이 호출되어야 함")
    void execute_ShouldDelegateToRegisterHandler() throws IOException {
        // Given
        RegisterCommand command = new RegisterCommand(registerHandler);
        // 실제 회원가입 메시지 예시 (형식은 중요하지 않지만 구색을 갖춤)
        String registerMessage = "REGISTER:user1,password,Name,Dept,Role";

        // When
        command.execute(registerMessage, bufferedReader);

        // Then
        // Command가 메시지를 변형하지 않고 핸들러에게 그대로 전달했는지 검증
        verify(registerHandler, times(1)).handle(registerMessage);
    }
}
