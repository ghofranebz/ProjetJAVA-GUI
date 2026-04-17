package services;

import entities.User;
import tools.Mydb;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService implements ICrud<User> {

    @Override
    public void add(User user) throws SQLException {
        String sql = "INSERT INTO users (first_name, last_name, email, password, phone, role, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setString(1, user.getFirstName());
            pst.setString(2, user.getLastName());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getPassword());
            pst.setString(5, user.getPhone());
            pst.setString(6, user.getRole());
            pst.setString(7, "PENDING");

            int affectedRows = pst.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("L'ajout de l'utilisateur a échoué, aucune ligne affectée.");
            }

            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    @Override
    public List<User> getAll() throws SQLException {
        String sql = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        }
        return users;
    }

    @Override
    public User getById(int id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        }
        return null;
    }

    @Override
    public void update(User user) throws SQLException {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, password = ?, phone = ?, role = ?, status = ? WHERE id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, user.getFirstName());
            pst.setString(2, user.getLastName());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getPassword());
            pst.setString(5, user.getPhone());
            pst.setString(6, user.getRole());
            pst.setString(7, user.getStatus());
            pst.setInt(8, user.getId());

            pst.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setInt(1, id);
            pst.executeUpdate();
        }
    }

    public void approveUser(int id) throws SQLException {
        changeStatus(id, "APPROVED");
    }

    public void rejectUser(int id) throws SQLException {
        changeStatus(id, "REJECTED");
    }

    public List<User> getUsersByStatus(String status) throws SQLException {
        String sql = "SELECT * FROM users WHERE status = ?";
        List<User> users = new ArrayList<>();
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, status);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        }
        return users;
    }

    private void changeStatus(int id, String status) throws SQLException {
        String sql = "UPDATE users SET status = ? WHERE id = ?";
        try (PreparedStatement pst = Mydb.getInstance().getConnection().prepareStatement(sql)) {
            pst.setString(1, status);
            pst.setInt(2, id);
            pst.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone"),
                rs.getString("role"),
                rs.getString("status")
        );
    }
}