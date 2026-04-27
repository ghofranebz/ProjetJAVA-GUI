package services;

import entities.User;
import tools.Mydb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private final Connection conn;

    public UserService() {
        this.conn = Mydb.getInstance().getConnection();
    }


    // ADD


    public void add(User user) throws SQLException {
        String sql = """
            INSERT INTO users
              (first_name, last_name, email, password, phone,
               address, city, postal_code, profile_photo, bio,
               role, status, rating_average, documents,
               created_at, updated_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS);

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
        ps.setString(11, user.getRole() != null
                ? user.getRole().name() : "CLIENT");
        ps.setString(12, user.getStatus() != null
                ? user.getStatus().name() : "PENDING_EMAIL");
        ps.setFloat(13,  user.getRatingAverage());
        ps.setString(14, user.getDocuments());
        ps.setTimestamp(15, Timestamp.valueOf(LocalDateTime.now()));
        ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));

        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) user.setId(keys.getInt(1));
        ps.close();
    }


    // GET ALL


    public List<User> getAll() throws SQLException {
        List<User> list = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT * FROM users ORDER BY created_at DESC");
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        st.close();
        return list;
    }


    // GET BY ID


    public User getById(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        User user = rs.next() ? mapRow(rs) : null;
        rs.close();
        ps.close();
        return user;
    }


    // GET BY STATUS


    public List<User> getUsersByStatus(String status) throws SQLException {
        List<User> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE status = ?");
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        ps.close();
        return list;
    }


    // GET BY ROLE


    public List<User> getUsersByRole(String role) throws SQLException {
        List<User> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM users WHERE role = ?");
        ps.setString(1, role);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        ps.close();
        return list;
    }


    // UPDATE


    public void update(User user) throws SQLException {
        String sql = """
            UPDATE users SET
              first_name=?, last_name=?, email=?, phone=?,
              address=?, city=?, postal_code=?, profile_photo=?,
              bio=?, role=?, status=?, documents=?, updated_at=?
            WHERE id=?
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
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
        ps.close();
    }


    // DELETE


    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM users WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }


    // actions admin


    public void approveUser(int id) throws SQLException {
        updateStatus(id, "APPROVED");
    }

    public void rejectUser(int id) throws SQLException {
        updateStatus(id, "REJECTED");
    }

    public void suspendUser(int id) throws SQLException {
        updateStatus(id, "SUSPENDED");
    }

    private void updateStatus(int id, String status) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "UPDATE users SET status=?, updated_at=? WHERE id=?");
        ps.setString(1, status);
        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(3, id);
        ps.executeUpdate();
        ps.close();
    }


    // maprow(con ligne sql > obj java)


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