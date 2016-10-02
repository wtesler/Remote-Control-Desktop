public class Main {


    public static void main(String[] args) {

        MouseSystemTray systemTray = new MouseSystemTray();
        systemTray.createTray();

        Server server = new Server();
        while (true) {
            server.waitForConnection();
        }
    }
}
