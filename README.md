# BANK_RMI
Aplikasi bank sederhana berbasis Java.
<br>
## Struktur Kode
### 1. `Bank.java (Antarmuka)`
Antarmuka Bank mendefinisikan metode yang dapat diakses oleh klien. Antarmuka ini mencakup berbagai fungsi perbankan, seperti pengecekan saldo, transfer dana, riwayat transaksi, dan login. Kami juga menambahkan metode baru untuk fitur penarikan tanpa kartu.

#### Kode
java

```bash
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface Bank extends Remote {
    double checkBalance(String accountNo, String pin) throws RemoteException;
    boolean transferFunds(String fromAccount, String toAccount, double amount, String pin) throws RemoteException;
    List<String> getTransactionHistory(String accountNo, String pin) throws RemoteException;
    boolean login(String accountNo, String pin) throws RemoteException;
    
    
    boolean cardlessWithdraw(String uniqueCode, double amount) throws RemoteException;
}
```

### Penjelasan
- `checkBalance`: Mengecek saldo akun dengan memverifikasi nomor akun dan PIN.
- `transferFunds`: Melakukan transfer dana antar akun jika nomor akun pengirim dan PIN valid.
- `getTransactionHistory`: Mendapatkan riwayat transaksi berdasarkan akun dan PIN.
- `login`: Memvalidasi nomor akun dan PIN.
- `cardlessWithdraw`: (Fitur Baru) Penarikan tanpa kartu menggunakan kode unik yang terdaftar pada akun.
### 2. `BankImpl.java (Implementasi Antarmuka)`
Kelas BankImpl adalah implementasi dari antarmuka Bank. Kelas ini mengimplementasikan logika bisnis dari semua metode perbankan dan menyimpan data sementara menggunakan struktur Map (peta) untuk saldo akun, PIN, kode unik, dan riwayat transaksi.

#### Kode
java
```bash
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
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


        accounts.put("CLIENT1", 7000.0);
        accounts.put("CLIENT2", 9000.0);
        pins.put("CLIENT1", "1234");
        pins.put("CLIENT2", "4567");
        
        uniqueCodes.put("0001", "CLIENT1");  
        uniqueCodes.put("0002", "CLIENT2");  

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
```

### Penjelasan
- ``checkBalance(String accountNo, String pin)``: Mengimplementasikan pengecekan saldo akun dengan mengakses data akun.
- ``transferFunds(String fromAccount, String toAccount, double amount, String pin)``: Mengimplementasikan transfer dana antara dua akun dan mengurangi saldo.
- ``getTransactionHistory(String accountNo, String pin)``: Mengimplementasikan riwayat transaksi dengan mengakses daftar transaksi yang disimpan.
- ``login(String accountNo, String pin)``: Mengimplementasikan fungsi login dengan memverifikasi akun dan PIN.
- ``cardlessWithdraw(String uniqueCode, double amount)``: Fitur Baru; memungkinkan penarikan tunai tanpa kartu menggunakan kode unik yang terdaftar pada akun.
- ``Konstruktur BankImpl()``: Inisialisasi akun, PIN, saldo awal, kode unik, dan riwayat transaksi.

### 3. `BankClient.java (Klien)`
Kelas BankClient menyediakan antarmuka konsol bagi pengguna untuk berinteraksi dengan aplikasi bank. Ini termasuk login, menu operasi bank, dan opsi penarikan tanpa kartu.

#### Kode
java
``` bash
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Scanner;

public class BankClient {

    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);
            Bank bank = (Bank) registry.lookup("BankService");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Masukkan nomor akun: ");
            String accountNo = scanner.nextLine();
            System.out.print("Masukkan PIN: ");
            String pin = scanner.nextLine();

            if (bank.login(accountNo, pin)) {
                String choice;

                do {
                    System.out.println("\n=== MENU BANK ===");
                    System.out.println("1. Cek Saldo");
                    System.out.println("2. Transfer Dana");
                    System.out.println("3. Lihat Riwayat Transaksi");
                    System.out.println("4. Keluar");
                    System.out.println("5. Penarikan Tanpa Kartu");
                    System.out.print("Pilih menu: ");
                    choice = scanner.nextLine();

                    switch (choice) {
                        case "1":
                            double balance = bank.checkBalance(accountNo, pin);
                            if (balance >= 0) {
                                System.out.println("Saldo: RP" + balance);
                            } else {
                                System.out.println("Saldo tidak ditemukan.");
                            }
                            break;

                        case "2":
                            System.out.print("Masukkan nomor akun penerima: ");
                            String toAccount = scanner.nextLine();
                            System.out.print("Masukkan jumlah: ");
                            double amount = scanner.nextDouble();
                            scanner.nextLine();

                            if (bank.transferFunds(accountNo, toAccount, amount, pin)) {
                                System.out.println("Transfer berhasil.");
                            } else {
                                System.out.println("Transfer gagal.");
                            }
                            break;

                        case "3":
                            List<String> history = bank.getTransactionHistory(accountNo, pin);
                            if (!history.isEmpty()) {
                                System.out.println("Riwayat Transaksi:");
                                for (String record : history) {
                                    System.out.println(record);
                                }
                            } else {
                                System.out.println("Tidak ada riwayat transaksi.");
                            }
                            break;

                        case "4":
                            System.out.println("Keluar...");
                            break;

                        case "5":
                            System.out.print("Masukkan kode unik: ");
                            String uniqueCode = scanner.nextLine();
                            System.out.print("Masukkan jumlah: ");
                            double withdrawAmount = scanner.nextDouble();
                            scanner.nextLine();

                            if (bank.cardlessWithdraw(uniqueCode, withdrawAmount)) {
                                System.out.println("Penarikan tanpa kartu berhasil.");
                            } else {
                                System.out.println("Penarikan tanpa kartu gagal.");
                            }
                            break;

                        default:
                            System.out.println("Pilihan tidak valid.");
                    }
                } while (!choice.equals("4"));
            } else {
                System.out.println("PIN salah. Akses ditolak.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```
### Penjelasan
- ``main(String[] args)``: Mengelola seluruh proses interaksi pengguna melalui antarmuka konsol, termasuk:
  - Login: Memverifikasi login pengguna.
  - Cek Saldo: Menampilkan saldo pengguna.
  - Transfer Dana: Memproses transfer dana antar-akun.
  - Lihat Riwayat Transaksi: Menampilkan riwayat transaksi pengguna.
  - Penarikan Tanpa Kartu: Memungkinkan pengguna melakukan penarikan tunai tanpa kartu melalui kode unik.

### 4. ``BankServer.java(Server)``
Kelas BankServer.java bertanggung jawab untuk memulai server RMI (Remote Method Invocation) dan membuat instance dari BankImpl, yaitu kelas implementasi yang menyediakan layanan perbankan sesuai antarmuka Bank. Kode ini mengikat instance BankImpl ke registry RMI, sehingga klien dapat mengakses metode yang disediakan oleh BankImpl melalui jaringan.

java

```bash
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class BankServer {
    public static void main(String[] args) {
        try {
            BankImpl bankImpl = new BankImpl();           
            Bank stub = (Bank) UnicastRemoteObject.exportObject(bankImpl, 0);            
            Registry registry = LocateRegistry.createRegistry(1099);            
            registry.rebind("BankService", stub);

            System.out.println("Bank server siap.");
        } catch (Exception e) {
            System.err.println("Bank server mengalami kesalahan: " + e.toString());
            e.printStackTrace();
        }
    }
}
```

### Penjelasan
- BankImpl dibuat sebagai instance. Kelas BankImpl ini merupakan implementasi dari antarmuka Bank, yang menyediakan semua logika bisnis perbankan, seperti pengecekan saldo, transfer, dan penarikan tanpa kartu.
- Dengan menggunakan UnicastRemoteObject.exportObject, bankImpl diekspor sebagai objek stub, yang kemudian dapat diakses secara remote. Stub ini adalah representasi remote dari objek bankImpl yang sesungguhnya.
- rebind digunakan untuk mengikat stub dengan nama BankService di registry RMI. Klien nantinya akan menggunakan nama ini (BankService) untuk mencari dan mengakses layanan bank dari server.
- e.printStackTrace() akan menampilkan detail kesalahan untuk memudahkan proses debugging.

## Fitur yang Tersedia

### 1. Cek Saldo
Menu ini memungkinkan pengguna untuk melihat saldo terkini dari akun mereka.
Pengguna memasukkan nomor akun dan PIN yang valid. Jika login berhasil, sistem akan menampilkan jumlah saldo yang tersimpan di akun tersebut.

#### ``Kode``
java

```bash
double balance = bank.checkBalance(accountNo, pin);
if (balance >= 0) {
    System.out.println("Saldo: $" + balance);
} else {
    System.out.println("Saldo tidak ditemukan.");
}
```
### 2. Transfer Dana
Menu ini memungkinkan pengguna untuk melakukan transfer uang ke akun lain.
Setelah memilih menu ini, pengguna memasukkan nomor akun tujuan, jumlah transfer, dan PIN. Jika data valid dan saldo mencukupi, transfer akan diproses.
Riwayat transfer ditambahkan ke daftar transaksi untuk akun pengirim dan penerima.

#### ``Kode``
java

```bash
if (bank.transferFunds(accountNo, toAccount, amount, pin)) {
    System.out.println("Transfer berhasil.");
} else {
    System.out.println("Transfer gagal.");
}
```

### 3. Lihat Riwayat Transaksi
Menu ini memungkinkan pengguna untuk melihat daftar transaksi yang dilakukan oleh akun mereka.
Pengguna akan diminta untuk memasukkan nomor akun dan PIN. Setelah verifikasi, sistem akan menampilkan riwayat transaksi berupa detail dari setiap transaksi yang dilakukan.

#### ``Kode``
java

```bash
List<String> history = bank.getTransactionHistory(accountNo, pin);
if (!history.isEmpty()) {
    System.out.println("Riwayat Transaksi:");
    for (String record : history) {
        System.out.println(record);
    }
} else {
    System.out.println("Tidak ada riwayat transaksi.");
}
```

### 4. Keluar
Menu ini memungkinkan pengguna untuk keluar dari sistem.
Setelah memilih menu ini, aplikasi akan mengakhiri sesi pengguna dan kembali ke layar awal atau menutup aplikasi.

#### ``Kode``
java

```bash
case "4":
    System.out.println("Keluar...");
    break;
```
### 5. Penarikan Tanpa Kartu (Fitur Baru)
Fitur ini memungkinkan pengguna untuk melakukan penarikan tunai tanpa kartu dengan menggunakan kode unik.
Pengguna harus memasukkan kode unik dan jumlah yang akan ditarik. Sistem akan memverifikasi apakah kode tersebut valid dan apakah saldo mencukupi untuk transaksi.
Jika valid, jumlah yang diminta akan dikurangi dari saldo akun, dan transaksi dicatat.

#### ``Kode``
java

```bash
if (bank.cardlessWithdraw(uniqueCode, withdrawAmount)) {
    System.out.println("Penarikan tanpa kartu berhasil.");
} else {
    System.out.println("Penarikan tanpa kartu gagal.");
}
```
