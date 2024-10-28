
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BankImpl extends UnicastRemoteObject implements Bank {

    private Map<String, Double> accounts;
    private Map<String, String> pins;
    private Map<String, String> uniqueCodes;
    private Map<String, List<String>> transactionHistory;

    protected BankImpl() throws RemoteException {
        accounts = new HashMap<>();
        pins = new HashMap<>();
        uniqueCodes = new HashMap<>();
        transactionHistory = new HashMap<>();

        // Inisialisasi akun dengan saldo awal, PIN, dan kode unik
        accounts.put("CLIENT1", 9000.0);
        accounts.put("CLIENT2", 6000.0);
        pins.put("CLIENT1", "1234");
        pins.put("CLIENT2", "4567");

        uniqueCodes.put("0000", "CLIENT1");  // Kode unik untuk CLIENT1
        uniqueCodes.put("0001", "CLIENT2");  // Kode unik untuk CLIENT2

        transactionHistory.put("CLIENT1", new ArrayList<>());
        transactionHistory.put("CLIENT2", new ArrayList<>());
    }

    @Override
    public boolean login(String accountNo, String pin) throws RemoteException {
        return pins.get(accountNo).equals(pin);
    }

    @Override
    public double checkBalance(String accountNo, String pin) throws RemoteException {
        if (login(accountNo, pin)) {
            Double balance = accounts.get(accountNo);
            if (balance != null) {
                return balance;
            }
        }
        return -1;
    }

    @Override
    public boolean transferFunds(String fromAccount, String toAccount, double amount, String pin) throws RemoteException {
        if (login(fromAccount, pin)) {
            Double fromBalance = accounts.get(fromAccount);
            Double toBalance = accounts.get(toAccount);

            if (fromBalance != null && fromBalance >= amount && toBalance != null) {
                accounts.put(fromAccount, fromBalance - amount);
                accounts.put(toAccount, toBalance + amount);
                transactionHistory.get(fromAccount).add("Transfer RP" + amount + " ke " + toAccount);
                transactionHistory.get(toAccount).add("Menerima RP" + amount + " dari " + fromAccount);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<String> getTransactionHistory(String accountNo, String pin) throws RemoteException {
        if (login(accountNo, pin)) {
            return transactionHistory.getOrDefault(accountNo, new ArrayList<>());
        }
        return new ArrayList<>();
    }

    @Override
    public boolean cardlessWithdraw(String uniqueCode, double amount) throws RemoteException {
        String accountNo = uniqueCodes.get(uniqueCode);

        if (accountNo != null) {
            Double balance = accounts.get(accountNo);
            if (balance != null && balance >= amount) {
                accounts.put(accountNo, balance - amount);
                transactionHistory.get(accountNo).add("Penarikan tanpa kartu sejumlah RP" + amount);
                return true;
            }
        }
        return false;
    }
}
