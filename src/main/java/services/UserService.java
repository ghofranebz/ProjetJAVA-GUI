package services;

import entities.User;
import tools.Mydb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService implements ICrud<User> {

    private final Connection conn;

    public UserService() {
        this.conn = Mydb.getInstance().getConnection();
    }


    // ─── ADD ──────────────────────────────────────────────────────────────────

    public void add(User user) throws SQLException {
        String sql = """
            INSERT INTO users
              (first_name, last_name, email, password, phone,
               address, city, postal_code, profile_photo, bio,
               role, status, rating_average, documents,
               created_at, updated_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,  user.getFirstName());
            ps.setString(2,  user.getLastName());
            ps.setString(3,  user.getEmail());
            ps.setString(4,  user.getPassword());
            ps.setString(5,  user.getPhone());
            ps.setString(6,  user.getAddress());
            ps.setString(7,  user.getCity());
            ps.setString(8,  user.getPostalCode());
            ps.setString(9,  user.getProfilePhoto());
            ps.setString(10, user.getBio());
            ps.setString(11, user.getRole()   != null ? user.getRole().name()   : "CLIENT");
            ps.setString(12, user.getStatus() != null ? user.getStatus().name() : "PENDING_EMAIL");
            ps.setFloat(13,  user.getRatingAverage());
            ps.setString(14, user.getDocuments());
            ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) user.setId(keys.getInt(1));
            }
        }
    }


    // ─── GET ALL ──────────────────────────────────────────────────────────────

    public List<User> getAll() throws SQLException {
        List<User> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM users ORDER BY created_at DESC")) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }


    // ─── GET BY ID ────────────────────────────────────────────────────────────

    public User getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }


    // ─── GET BY STATUS ────────────────────────────────────────────────────────

    public List<User> getUsersByStatus(String status) throws SQLException {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE status = ? ORDER BY created_at DESC")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<User> getPendingUsers() throws SQLException {
        List<User> list = new ArrayList<>();
        String sql = """
            SELECT * FROM users
            WHERE status IN ('PENDING_EMAIL', 'PENDING_ADMIN')
            ORDER BY created_at DESC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        }
        return list;
    }

    public List<User> getApprovedUsers() throws SQLException {
        return getUsersByStatus("APPROVED");
    }

    public List<User> getRejectedUsers() throws SQLException {
        return getUsersByStatus("REJECTED");
    }

    public List<User> getSuspendedUsers() throws SQLException {
        return getUsersByStatus("SUSPENDED");
    }


    // ─── GET BY ROLE ──────────────────────────────────────────────────────────

    public List<User> getUsersByRole(String role) throws SQLException {
        List<User> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE role = ?")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    // ─── FIND BY EMAIL + PASSWORD (login) ─────────────────────────────────────

    public Optional<User> findByEmailAndPassword(String email, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        }
    }


    // ─── SEARCH ───────────────────────────────────────────────────────────────

    public List<User> searchUsers(String query) throws SQLException {
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) return getAll();
        String sql = """
            SELECT * FROM users
            WHERE LOWER(first_name) LIKE ?
               OR LOWER(last_name)  LIKE ?
               OR LOWER(email)      LIKE ?
               OR LOWER(city)       LIKE ?
               OR LOWER(address)    LIKE ?
            ORDER BY created_at DESC
            """;
        List<User> list = new ArrayList<>();
        String pattern = "%" + q + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 1; i <= 5; i++) ps.setString(i, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }


    // ─── UPDATE ───────────────────────────────────────────────────────────────

    public void update(User user) throws SQLException {
        String sql = """
            UPDATE users SET
              first_name=?, last_name=?, email=?, phone=?,
              address=?, city=?, postal_code=?, profile_photo=?,
              bio=?, role=?, status=?, documents=?, updated_at=?
            WHERE id=?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1,  user.getFirstName());
            ps.setString(2,  user.getLastName());
            ps.setString(3,  user.getEmail());
            ps.setString(4,  user.getPhone());
            ps.setString(5,  user.getAddress());
            ps.setString(6,  user.getCity());
            ps.setString(7,  user.getPostalCode());
            ps.setString(8,  user.getProfilePhoto());
            ps.setString(9,  user.getBio());
            ps.setString(10, user.getRole().name());
            ps.setString(11, user.getStatus().name());
            ps.setString(12, user.getDocuments());
            ps.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(14, user.getId());
            ps.executeUpdate();
        }
    }


    // ─── DELETE ───────────────────────────────────────────────────────────────

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }


    // ─── ADMIN ACTIONS ────────────────────────────────────────────────────────

    public void approveUser(int id) throws SQLException { updateStatus(id, "APPROVED");  }
    public void rejectUser(int id)  throws SQLException { updateStatus(id, "REJECTED");  }
    public void suspendUser(int id) throws SQLException { updateStatus(id, "SUSPENDED"); }

    private void updateStatus(int id, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET status=?, updated_at=? WHERE id=?")) {
            ps.setString(1, status);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, id);
            ps.executeUpdate();
        }
    }


    // ─── STATS ────────────────────────────────────────────────────────────────

    public int countByStatus(String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE status = ?")) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countByRole(String role) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE role = ?")) {
            ps.setString(1, role);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countTotal() throws SQLException {
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM users")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public int countRecentRegistrations(int days) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM users WHERE created_at >= DATE_SUB(NOW(), INTERVAL ? DAY)")) {
            ps.setInt(1, days);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }


    // ─── mapRow : ResultSet → User ────────────────────────────────────────────

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("email"),
                rs.getString("password"),
                rs.getString("phone"),
                rs.getString("address"),
                rs.getString("city"),
                rs.getString("postal_code"),
                rs.getString("profile_photo"),
                rs.getString("bio"),
                rs.getString("role"),
                rs.getString("status"),
                rs.getFloat("rating_average"),
                rs.getString("documents"),
                rs.getTimestamp("email_verified_at") != null
                        ? rs.getTimestamp("email_verified_at").toLocalDateTime() : null,
                rs.getTimestamp("last_login_at") != null
                        ? rs.getTimestamp("last_login_at").toLocalDateTime() : null,
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }
}
