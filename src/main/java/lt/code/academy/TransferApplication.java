package lt.code.academy;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import lt.code.academy.client.MongoObjectClientProvider;
import lt.code.academy.data.Transaction;
import lt.code.academy.data.User;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;


import java.util.Scanner;

public class TransferApplication {

    private final MongoCollection<User> usersCollection;
    private final MongoCollection<Transaction> transactionsCollection;

    public TransferApplication(MongoCollection<User> usersCollection, MongoCollection<Transaction> transactionsCollection) {
        this.usersCollection = usersCollection;
        this.transactionsCollection = transactionsCollection;
    }

    public static void main(String[] args) {
        MongoClient client = MongoObjectClientProvider.getClient();
        MongoDatabase database = client.getDatabase("transferapp");
        TransferApplication application = new TransferApplication(database.getCollection("users", User.class),
                database.getCollection("transactions", Transaction.class));

        Scanner scanner = new Scanner(System.in);

        String action;

        do {
            application.menu();
            action = scanner.nextLine();

            application.userSelection(scanner, action);
        } while (!action.equals("3"));

        scanner.close();
    }

    private void menu() {
        System.out.println("""
                1. Registration
                2. Money transfer
                3. Exit
                """);
    }

    private void userSelection(Scanner scanner, String action) {
        switch (action) {
            case "1" -> createUser(scanner);
            case "2" -> transferMoney(scanner);
            case "3" -> System.out.println("Exit");
            default -> System.out.println("Such action does not exist");
        }
    }

    private void createUser(Scanner scanner) {
        System.out.println("Enter your name");
        String name = scanner.nextLine();

        System.out.println("Enter your surname");
        String surname = scanner.nextLine();

        System.out.println("Enter how much money you want to deposit within your registration");
        double balance = Double.parseDouble(scanner.nextLine());

        User user = new User(null, name, surname, balance);

        usersCollection.insertOne(user);

    }

    private void transferMoney(Scanner scanner) {
        System.out.println("Enter the sender name");
        String senderName = scanner.nextLine();

        User sender = usersCollection.find(eq("name", senderName)).first();

        if (sender.getName() == null) {
            System.out.println("Incorrect sender name");
            return;
        }

        System.out.println("Enter the receiver name");
        String receiverName = scanner.nextLine();

        User receiver = usersCollection.find(eq("name", receiverName)).first();

        if (receiver.getName() == null) {
            System.out.println("Incorrect receiver name");
            return;
        }

        System.out.println("Enter how much do you want to transfer");
        double amount = Double.parseDouble(scanner.nextLine());

        double senderBalance = sender.getBalance() - amount;
        usersCollection.updateOne(eq("_id", sender.getId()), set("balance", senderBalance));
        System.out.println(sender.getBalance());

        if (senderBalance < 0) {
            System.out.println("Not enough money");
            return;
        }

        double receiverBalance = receiver.getBalance() + amount;

        usersCollection.updateOne(eq("_id", receiver.getId()), set("balance", receiverBalance));


        Transaction transaction = new Transaction(sender.getName(), receiver.getName(), amount);
        transactionsCollection.insertOne(transaction);

        System.out.printf("User %s %s sent %s $ to %s %s", sender.getName(), sender.getSurname(), amount, receiver.getName(), receiver.getSurname());
    }
}
