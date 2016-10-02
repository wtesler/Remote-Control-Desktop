import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private Robot mRobot;
    private ServerSocket mServerSocket;

    private int mouseX, mouseY;

    public Server() {
        try {
            mRobot = new Robot();
            mServerSocket = new ServerSocket(63288);
            System.out.println("Server on port: " + mServerSocket.getLocalPort());
        } catch (IOException | AWTException e) {
            e.printStackTrace();
        }
    }

    public void waitForConnection() {
        try {
            System.out.println("Waiting for connection.");
            Socket clientSocket = mServerSocket.accept();
            System.out.println("Connected to client.");
            listen(clientSocket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getLocalPort() {
        return mServerSocket.getLocalPort();
    }

    private void listen(Socket clientSocket) {
        try {
            DataInputStream inStream = new DataInputStream(clientSocket.getInputStream());

            int inVal;
            while (true) {
                inVal = inStream.readInt();
                switch (inVal) {
                    case ProtocolConstants.CODE_CALIBRATE:
                        centerMouse();
                        break;
                    case ProtocolConstants.CODE_LEFT_CLICK_DOWN:
                        mRobot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_LEFT_CLICK_UP:
                        mRobot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_CLICK_DOWN:
                        mRobot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    case ProtocolConstants.CODE_RIGHT_CLICK_UP:
                        mRobot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
                        break;
                    default:
                        mouseX -= inVal;
                        mouseY -= inStream.readInt();
                        mRobot.mouseMove(mouseX, mouseY);
                }
            }
        } catch (EOFException e) {
            System.out.println("Connection with client was terminated.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void centerMouse() {
        mouseX = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 2;
        mouseY = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 2;
        mRobot.mouseMove(mouseX, mouseY);
    }
}
