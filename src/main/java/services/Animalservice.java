package services;

import entities.Animal;
import tools.Mydb;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnimalService {

    private final Connection conn;

    public AnimalService() {
        this.conn = Mydb.getInstance().getConnection();
    }

    // ========================
    // ADD
    // ========================

    public void add(Animal animal) throws SQLException {
        String sql = """
            INSERT INTO animals
              (owner_id, name, species, breed, birth_date, gender,
               weight, color, availability_status, is_neutered,
               microchip_number, photo, created_at, updated_at)
            VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;
        PreparedStatement ps = conn.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS);

        ps.setInt(1, animal.getOwnerId());
        ps.setString(2, animal.getName());
        ps.setString(3, animal.getSpecies());
        ps.setString(4, animal.getBreed());
        ps.setDate(5, animal.getBirthDate() != null
                ? Date.valueOf(animal.getBirthDate()) : null);
        ps.setString(6, animal.getGender());
        ps.setFloat(7, animal.getWeight());
        ps.setString(8, animal.getColor());
        ps.setString(9, animal.getAvailabilityStatus() != null
                ? animal.getAvailabilityStatus().name() : "PENDING");
        ps.setBoolean(10, animal.isNeutered());
        ps.setString(11, animal.getMicrochipNumber());
        ps.setString(12, animal.getPhoto());
        ps.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));
        ps.setTimestamp(14, Timestamp.valueOf(LocalDateTime.now()));

        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        if (keys.next()) animal.setId(keys.getInt(1));
        ps.close();
    }

    // ========================
    // GET ALL
    // ========================

    public List<Animal> getAll() throws SQLException {
        List<Animal> list = new ArrayList<>();
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT * FROM animals ORDER BY created_at DESC");
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        st.close();
        return list;
    }

    // ========================
    // GET BY ID
    // ========================

    public Animal getById(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM animals WHERE id = ?");
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Animal animal = rs.next() ? mapRow(rs) : null;
        rs.close();
        ps.close();
        return animal;
    }

    // ========================
    // GET BY OWNER
    // ========================

    public List<Animal> getByOwnerId(int ownerId) throws SQLException {
        List<Animal> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM animals WHERE owner_id = ?");
        ps.setInt(1, ownerId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        ps.close();
        return list;
    }

    // ========================
    // GET BY STATUS
    // ========================

    public List<Animal> getByStatus(String status) throws SQLException {
        List<Animal> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM animals WHERE availability_status = ?");
        ps.setString(1, status);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        ps.close();
        return list;
    }

    // ========================
    // GET BY SPECIES
    // ========================

    public List<Animal> getBySpecies(String species) throws SQLException {
        List<Animal> list = new ArrayList<>();
        PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM animals WHERE species = ?");
        ps.setString(1, species);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapRow(rs));
        rs.close();
        ps.close();
        return list;
    }

    // ========================
    // UPDATE
    // ========================

    public void update(Animal animal) throws SQLException {
        String sql = """
            UPDATE animals SET
              name=?, species=?, breed=?, birth_date=?, gender=?,
              weight=?, color=?, availability_status=?, is_neutered=?,
              microchip_number=?, photo=?, updated_at=?
            WHERE id=?
            """;
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, animal.getName());
        ps.setString(2, animal.getSpecies());
        ps.setString(3, animal.getBreed());
        ps.setDate(4, animal.getBirthDate() != null
                ? Date.valueOf(animal.getBirthDate()) : null);
        ps.setString(5, animal.getGender());
        ps.setFloat(6, animal.getWeight());
        ps.setString(7, animal.getColor());
        ps.setString(8, animal.getAvailabilityStatus().name());
        ps.setBoolean(9, animal.isNeutered());
        ps.setString(10, animal.getMicrochipNumber());
        ps.setString(11, animal.getPhoto());
        ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));
        ps.setInt(13, animal.getId());
        ps.executeUpdate();
        ps.close();
    }

    // ========================
    // DELETE
    // ========================

    public void delete(int id) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM animals WHERE id = ?");
        ps.setInt(1, id);
        ps.executeUpdate();
        ps.close();
    }

    // ========================
    // MAPPER
    // ========================

    private Animal mapRow(ResultSet rs) throws SQLException {
        return new Animal(
                rs.getInt("id"),
                rs.getInt("owner_id"),
                rs.getString("name"),
                rs.getString("species"),
                rs.getString("breed"),
                rs.getDate("birth_date") != null
                        ? rs.getDate("birth_date").toLocalDate() : null,
                rs.getString("gender"),
                rs.getFloat("weight"),
                rs.getString("color"),
                rs.getString("availability_status"),
                rs.getBoolean("is_neutered"),
                rs.getString("microchip_number"),
                rs.getString("photo"),
                rs.getTimestamp("created_at") != null
                        ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                rs.getTimestamp("updated_at") != null
                        ? rs.getTimestamp("updated_at").toLocalDateTime() : null
        );
    }
}