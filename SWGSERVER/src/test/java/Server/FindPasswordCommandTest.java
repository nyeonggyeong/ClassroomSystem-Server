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
class FindPasswordCommandTest {

    @Mock
    LoginManager loginManager;

    @Mock
    BufferedReader bufferedReader; // execute 메서드 파라미터 맞추기용 (사용은 안 함)

    @Test
    @DisplayName("비밀번호 찾기 데이터가 존재할 때 - Manager 정상 호출")
    void execute_ShouldCallCheckPassword_WhenDataIsPresent() throws IOException {
        // Given
        FindPasswordCommand command = new FindPasswordCommand(loginManager);
        
        // 예: "FIND_PASSWORD:" 뒤에 "userid,email" 같은 정보가 붙어오는 상황
        String validData = "user123,user@example.com";
        String message = "FIND_PASSWORD:" + validData;

        // When
        command.execute(message, bufferedReader);

        // Then: 접두어를 제외한 순수 데이터가 Manager로 전달되었는지 검증
        verify(loginManager, times(1)).checkPassword(validData);
    }

    @Test
    @DisplayName("데이터가 비어있을 때 - Manager 호출하지 않고 종료")
    void execute_ShouldDoNothing_WhenDataIsEmpty() throws IOException {
        // Given
        FindPasswordCommand command = new FindPasswordCommand(loginManager);
        
        // 데이터 없이 접두어만 온 상황
        String message = "FIND_PASSWORD:";

        // When
        command.execute(message, bufferedReader);

        // Then: 데이터가 없으므로 LoginManager의 메서드는 절대 호출되면 안 됨
        verify(loginManager, never()).checkPassword(anyString());
    }
}