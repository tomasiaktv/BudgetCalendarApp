package repository;

import model.Transaction;

import java.nio.file.Path;
import java.util.List;

public interface TransactionRepository {

    void save(List<Transaction> transactions, Path filePath);

    List<Transaction> load(Path filePath);
}