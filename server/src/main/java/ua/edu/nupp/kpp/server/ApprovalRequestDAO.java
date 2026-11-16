package ua.edu.nupp.kpp.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ApprovalRequestDAO {

    private static final String COLUMNS = "id,document_id,reviewer_id,state,f1,f2,f3,priority_index,created_at";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void add(ApprovalRequest a) throws SQLException {
        String sql = "INSERT INTO approval_request(document_id,reviewer_id,state,f1,f2,f3,priority_index) VALUES(?,?,?,?,?,?,?)";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, a);
            p.executeUpdate();
        }
    }

    public List<ApprovalRequest> getAll() throws SQLException {
        List<ApprovalRequest> list = new ArrayList<>();
        String sql = "SELECT " + COLUMNS + " FROM approval_request ORDER BY id";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            while (r.next()) {
                list.add(map(r));
            }
        }
        return list;
    }

    public ApprovalRequest getById(long id) throws SQLException {
        String sql = "SELECT " + COLUMNS + " FROM approval_request WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? map(r) : null;
            }
        }
    }

    public void update(ApprovalRequest a) throws SQLException {
        String sql = "UPDATE approval_request SET document_id=?,reviewer_id=?,state=?,f1=?,f2=?,f3=?,priority_index=? WHERE id=?";
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement(sql)) {
            bind(p, a);
            p.setLong(8, a.getId());
            p.executeUpdate();
        }
    }

    public void updateState(long id, ApprovalState state) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("UPDATE approval_request SET state=? WHERE id=?")) {
            p.setString(1, state.name());
            p.setLong(2, id);
            p.executeUpdate();
        }
    }

    public void delete(long id) throws SQLException {
        try (Connection c = DBUtil.getConnection(); PreparedStatement p = c.prepareStatement("DELETE FROM approval_request WHERE id=?")) {
            p.setLong(1, id);
            p.executeUpdate();
        }
    }

    private void bind(PreparedStatement p, ApprovalRequest a) throws SQLException {
        p.setLong(1, a.getDocumentId());
        p.setLong(2, a.getReviewerId());
        p.setString(3, a.getState().name());
        p.setDouble(4, a.getF1());
        p.setDouble(5, a.getF2());
        p.setDouble(6, a.getF3());
        p.setDouble(7, a.getPriorityIndex());
    }

    private ApprovalRequest map(ResultSet r) throws SQLException {
        Timestamp created = r.getTimestamp("created_at");
        return new ApprovalRequest(r.getLong("id"), r.getLong("document_id"), r.getLong("reviewer_id"),
                ApprovalState.valueOf(r.getString("state")), r.getDouble("f1"), r.getDouble("f2"), r.getDouble("f3"),
                r.getDouble("priority_index"), created == null ? null : created.toLocalDateTime().format(TIME));
    }
}
