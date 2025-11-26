/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author user
 */
import Server.SessionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedReader;
import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancelReservationCommandTest {
    @Mock
    SessionManager sessionManager;
    
    @Mock
    BufferedReader bufferedReader;
    
    @Test
    void execute_ShouldCallCancelReservation() throws IOException {
        CancelReservationCommand command = new CancelReservationCommand(sessionManager);
        
        command.execute("CANCEL_RESERVATION:testUser", bufferedReader);
        
        verify(sessionManager, times(1)).addCancelUser("testUser");
    }
}
