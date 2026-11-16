package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReviewerDAO {

    public void add(Reviewer v) throws SQLException {
        String sql = "INSERT INTO reviewer(full_name,job_title,email) VALUES(?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, v);
            p.executeUpdate();
        }
    }

    public List<Reviewer> getAll() throws SQLException {
        List<Reviewer> list = new ArrayList<>();
        String sql = "SELECT id,full_name,job_title,email FROM reviewer ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Reviewer getById(long id) throws SQLException {
        String sql = "SELECT id,full_name,job_title,email FROM reviewer WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Reviewer v) throws SQLException {
        String sql = "UPDATE reviewer SET full_name=?,job_title=?,email=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, v);
            p.setLong(4, v.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM reviewer WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Reviewer v) throws SQLException {
        p.setString(1, v.getFullName());
        p.setString(2, v.getJobTitle());
        p.setString(3, v.getEmail());
    }

    private Reviewer map(ResultSet r) throws SQLException {
        return new Reviewer(r.getLong("id"), r.getString("full_name"), r.getString("job_title"), r.getString("email"));
    }
}
