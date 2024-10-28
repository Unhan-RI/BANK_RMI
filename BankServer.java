import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class BankServer {
    public static void main(String[] args) {
        try {
            // Membuat objek implementasi bank
            BankImpl bank = new BankImpl();

            // Memulai registry RMI
            Registry registry = LocateRegistry.createRegistry(1099);

            // Daftarkan BankService ke registry
            registry.rebind("BankService", bank);

            System.out.println("Bank Server siap.");

        } catch (Exception e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
