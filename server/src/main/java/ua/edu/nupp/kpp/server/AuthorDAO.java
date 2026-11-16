package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AuthorDAO {

    public void add(Author a) throws SQLException {
        String sql = "INSERT INTO author(full_name,department,email) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, a);
            p.executeUpdate();
        }
    }

    public List<Author> getAll() throws SQLException {
        List<Author> list = new ArrayList<>();
        String sql = "SELECT id,full_name,department,email FROM author ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Author getById(long id) throws SQLException {
        String sql = "SELECT id,full_name,department,email FROM author WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Author a) throws SQLException {
        String sql = "UPDATE author SET full_name=?,department=?,email=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, a);
            p.setLong(4, a.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM author WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Author a) throws SQLException {
        p.setString(1, a.getFullName());
        p.setString(2, a.getDepartment());
        p.setString(3, a.getEmail());
    }

    private Author map(ResultSet r) throws SQLException {
        return new Author(r.getLong("id"), r.getString("full_name"), r.getString("department"), r.getString("email"));
    }
}
