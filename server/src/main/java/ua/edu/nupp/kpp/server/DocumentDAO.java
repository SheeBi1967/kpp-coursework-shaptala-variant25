package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DocumentDAO {

    private static final String COLUMNS = "id,author_id,reg_number,title,doc_type,due_date,criticality,dependent_processes";

    public void add(Document d) throws SQLException {
        String sql = "INSERT INTO document(author_id,reg_number,title,doc_type,due_date,criticality,dependent_processes) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, d);
            p.executeUpdate();
        }
    }

    public List<Document> getAll() throws SQLException {
        List<Document> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM document ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public Document getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM document WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public Document getByRegNumber(String regNumber) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM document WHERE reg_number=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, regNumber);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(Document d) throws SQLException {
        String sql = "UPDATE document SET author_id=?,reg_number=?,title=?,doc_type=?,due_date=?,criticality=?,dependent_processes=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, d);
            p.setLong(8, d.getId());
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM document WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, Document d) throws SQLException {
        p.setLong(1, d.getAuthorId());
        p.setString(2, d.getRegNumber());
        p.setString(3, d.getTitle());
        p.setString(4, d.getDocType());
        p.setDate(5, Date.valueOf(d.getDueDate()));
        p.setInt(6, d.getCriticality());
        p.setInt(7, d.getDependentProcesses());
    }

    private Document map(ResultSet r) throws SQLException {
        return new Document(r.getLong("id"), r.getLong("author_id"), r.getString("reg_number"), r.getString("title"),
                r.getString("doc_type"), r.getDate("due_date").toLocalDate().toString(),
                r.getInt("criticality"), r.getInt("dependent_processes"));
    }
}
