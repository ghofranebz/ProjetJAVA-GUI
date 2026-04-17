package services;

import java.sql.SQLException;
import java.util.List;

public interface ICrud<T> {
    void add(T entity) throws SQLException;
    List<T> getAll() throws SQLException;
    T getById(int id) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(int id) throws SQLException;
}